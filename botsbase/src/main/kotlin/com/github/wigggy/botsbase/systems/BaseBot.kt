package com.github.wigggy.botsbase.systems


import com.github.wigggy.botsbase.systems.data.OptionPositionDb
import com.github.wigggy.botsbase.systems.bot_tools.ColorLogger
import com.github.wigggy.botsbase.systems.bot_tools.Common
import com.github.wigggy.botsbase.systems.bot_tools.MarketTimeUtil
import com.github.wigggy.botsbase.systems.bot_tools.collectLatestSafe
import com.github.wigggy.botsbase.systems.data.BotStateDb
import com.github.wigggy.botsbase.systems.data.data_objs.AnalysisResult
import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import com.github.wigggy.botsbase.systems.managers.AccountManagerPapertrade
import com.github.wigggy.botsbase.systems.managers.OrderManagerPapertrade
import com.github.wigggy.botsbase.systems.managers.PosUpdateManager
import com.github.wigggy.botsbase.tools.Log
import com.github.wigggy.botsbase.tools.calculatePercentageGain
import com.github.wigggy.botsbase.tools.doubleToTwoDecimalFormat
import com.github.wigggy.botsbase.tools.safeAverage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

/* TODO
*   - Revamp this so that the switch to real money is easier.
*       - Particularly the balances
*           - getCashBal()
*           - getBuyingPower()
*           - getTotalBal()
* */

/* Notes
 * To start/run this you need to call .start()
 * */

/* TODO
*   - Add avgCycleTime to BotState
* */
abstract class BaseBot(
    val botName: String,
    private val botDesc: String,
    private val dateBotCreatedMMDDYYYY: String,
    private val startingPapertradeBalance: Double,
    private val postCycleSleepTimeMs: Long
): Thread() {

    // Basics
    private val log = ColorLogger(botName)
    private val coroutineScopeIO = CoroutineScope(Dispatchers.IO)
    private val coroutineScopePosUpdate = CoroutineScope(buildPosUpdateCoroutineDispatcher())
    protected val csApi = Common.csApi
    
    // State                                    // Initialize with basic info -- will be loaded in run()
    private val _botState: MutableStateFlow<BotState> = MutableStateFlow(BotState(botName=botName, botDesc=botDesc))
    val botState: StateFlow<BotState> get() = _botState
    private var cycleCount = 0

    // Flags
    private var power: AtomicBoolean = AtomicBoolean(false)
    private var botStateInitialized = false
    private var initBotRan = false
    private var botThreadShutdown = AtomicBoolean(false)

    // Core Helper Classes
    protected val botStateDb: BotStateDb = BotStateDb(botName)
    protected val posDb: OptionPositionDb = OptionPositionDb(botName)
    protected val orderManager = OrderManagerPapertrade(posDb)
    protected val accountManager = AccountManagerPapertrade(posDb, startingPapertradeBalance)


    init {

        // Add Runtime Shutdown Hook
        addShutdownHook()
    }
    
    
    
    // Functions Start ////////////////////////////////////////////////////////////////////////////////////////////////


    private fun buildPosUpdateCoroutineDispatcher(): CoroutineDispatcher {
        return object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                Thread(block).start()
            }
        }
    }


    /** Runs basic init of Bot.
     *
     * Runs once per program start. */
    private fun initBot() {

        // Check flag for if bot has already ran initBot()
        if (initBotRan) return

        // Load bot state
        loadBotState()

        resetBlacklistForDay()
        resetWatchlistForDay()

        // Update bot status (open/closed positiosn, gain/loss, etc...)
        updateBot()

        // Update balance and buying power
        updateDaysStartingBalances()

        // Set flag
        initBotRan = true

        log.dbug("initBot() Ran.")

        return
    }


    /** Attempts to load BotState from DB and updates days starting balance. Creates BotState on failure (New Bot).
     *
     *  Called once per program start.*/
    private fun loadBotState() {

        // Check flag to see if it has already been loaded since program started
        if (botStateInitialized) return

        // Try to load from db, if successful update state flow and return
        val bs = botStateDb.getBotState()
        if (bs != null){
            _botState.value = bs
            botStateInitialized = true
            log.dbug("loadBotState() Ran.")
            return
        }

        // Initialize a new BotState. // initNewBotState also saves it to database for future
        val newBs = botStateDb.initNewBotState(
            botName, botDesc, dateBotCreatedMMDDYYYY, startingPapertradeBalance, postCycleSleepTimeMs,
        )
        _botState.value = newBs
        botStateInitialized = true
        log.dbug("loadBotState() Ran.")
    }


    /** Saves botstate in DB and updates the StateFlow<BotState> */
    protected fun saveBotState(state: BotState) {
        val t = System.currentTimeMillis()
        val d = Date(t)
        val newState = state.copy(
            lastUpdateTimestampMs = t,
            lastUpdateDate = d
        )
        botStateDb.updateBotState(newState)
        _botState.update {
            newState
        }
        log.dbug("updateBotState() Ran.")
    }


    /** Updates BotState's curPapertradeBalance and daysStartingBalance.
     *
     * Runs once per day. */
    private fun updateDaysStartingBalances() {
        // Get the start of the current day in millis
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Check if daysStartingBal has been updated today, return if so
        val lastUpdated = botState.value.daysStartingBalanceLastUpdate
        if (lastUpdated > startOfDay) {
            return
        }

        // Get balance and update state
        val curCashBal = getCashBalance()
        val curTime = System.currentTimeMillis()
        val newState = _botState.value.copy(
            curCashPapertradeBalance = curCashBal,
            daysStartingBalance = curCashBal,
            daysStartingBalanceLastUpdate = curTime,
            curTotalBalance = getTotalBalance(),
            buyingPower = getBuyingPower()
        )
        log.dbug("updateDaysStartingBalances() Ran. Current Cash Bal: $curCashBal")
        saveBotState(newState)
    }


    private fun resetBlacklistForDay() {
        // Get the start of the current day in millis
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // blacklist
        if (_botState.value.blackListLastUpdatedMs < startOfDay) {
            saveBotState(_botState.value.copy(
                tickerBlackList = listOf(), blackListLastUpdatedMs = System.currentTimeMillis())
            )
        }

        // watchlist
        if (_botState.value.watchlistLastUpdateMs < startOfDay) {
            saveBotState(_botState.value.copy(
                watchlist = listOf(), watchlistLastUpdateMs = System.currentTimeMillis()
            ))
        }
    }


    private fun resetWatchlistForDay() {
        // Get the start of the current day in millis
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // watchlist
        if (_botState.value.watchlistLastUpdateMs < startOfDay) {
            saveBotState(_botState.value.copy(
                watchlist = listOf(), watchlistLastUpdateMs = System.currentTimeMillis()
            ))
        }
    }


    // TODO -- BotState.tradePermCheck No longer needed. Remove from bot state
//    /** Updates the Trade Permission in BotState */
//    private fun updateTradePermission(tradePermissionCheckResults: TradePermissionCheckResults) {
//        val s = _botState.value.copy(
//            tradePermCheck = tradePermissionCheckResults
//        )
//        saveBotState(s)
//    }


    /** Returns the available buying power for bot. Updates botstate.buyingPower
     *
     * The total cost of positions opened today is subtracted from the 'botState.daysStartingBalance' */
    protected fun getBuyingPower(): Double {
        val bp = accountManager.getBuyingPower()
        saveBotState(
            _botState.value.copy(buyingPower = bp)
        )
        return bp
    }


    /** Returns the balance of account including Papertrade balance (Mock Cash) and Open Positions Value */
    protected fun getTotalBalance(): Double {
        val tb =accountManager.getTotalBalance()
        saveBotState(
            _botState.value.copy(curTotalBalance = tb)
        )
        return tb
    }


    /** Returns Papertrade balance. Open positions not included */
    protected fun getCashBalance(): Double {
        val cb = accountManager.getCashBalance()
        saveBotState(
            _botState.value.copy(curCashPapertradeBalance = cb)
        )
        return cb
    }


    // TODO -- Go Over This Again
    private fun updateBot() {

        val getNPosOpenedToday = {listOfPositions: List<OptionPosition> ->

            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val posOpenedToday = listOfPositions.filter { it.openTimestampMs > startOfDay }
            posOpenedToday.size
        }

        val daysPosList = posDb.getAllPositionsOfDay()

        val open = daysPosList.filter { it.closeTimestampMs == 0L }
        val closed = daysPosList.filter { it.closeTimestampMs != 0L }
        val realGainList = mutableListOf<Double>()
        val realPctList = mutableListOf<Double>()
        val winsListDollar = mutableListOf<Double>()
        val winsListPercent = mutableListOf<Double>()
        val lossesListDollar = mutableListOf<Double>()
        val lossesListPercent = mutableListOf<Double>()
        var gDolDaysRealized = 0.0      // Closed only
        var gDolDaysUnrealized = 0.0    // Open only
        var gDolDaysTotal = 0.0
        var totalCostOfClosedPos = 0.0
        var totalValueOfClosedPos = 0.0

        val daysTradedTickers = mutableListOf<String>()
        val daysTickerGainMap = mutableMapOf<String, Double>()
        var topGainingTicker = ""
        var topGainingTickerAmount = 0.0

        for (p in daysPosList) {

            // Closed
            if (p.closeTimestampMs != 0L) {
                realGainList.add(p.gainLossDollarTotal)
                gDolDaysRealized += p.gainLossDollarTotal
                gDolDaysTotal += p.gainLossDollarTotal
                totalCostOfClosedPos += p.totalPrice
                totalValueOfClosedPos += p.curValueOfPosition
                realPctList.add(p.gainLossPercent)

                // Win
                if (p.gainLossDollarTotal >= 0.0){
                    winsListDollar.add(p.gainLossDollarTotal)
                    winsListPercent.add(p.gainLossPercent)
                }
                // Loss
                else {
                    lossesListDollar.add(p.gainLossDollarTotal)
                    lossesListPercent.add(p.gainLossPercent)
                }

                // Ticker Gain Map (Closed Pos Only) does not contain symbol yet
                if (daysTickerGainMap.keys.contains(p.stockSymbol) == false) {
                    val newGain = p.gainLossDollarTotal
                    daysTickerGainMap[p.stockSymbol] = newGain
                }
                // Ticker Gain Map (Closed Pos Only) contains ticker already
                else {
                    val newGain = daysTickerGainMap[p.stockSymbol]!! + p.gainLossDollarTotal
                    daysTickerGainMap[p.stockSymbol] = newGain
                }

                // Top Gaining Ticker
                if (p.gainLossDollarTotal > topGainingTickerAmount) {
                    topGainingTicker = p.stockSymbol
                    topGainingTickerAmount = p.gainLossDollarTotal
                }
            }

            // Open
            if (p.closeTimestampMs == 0L) {
                gDolDaysUnrealized += p.gainLossDollarTotal
                gDolDaysTotal += p.gainLossDollarTotal
            }

            // Days Traded Tickers LIST
            if (!daysTradedTickers.contains(p.stockSymbol)) {
                daysTradedTickers.add(p.stockSymbol)
            }



        }
        val gPctRealized = calculatePercentageGain(totalCostOfClosedPos, totalValueOfClosedPos)
        val gDolAvg = realGainList.safeAverage()
        val gPctAvg = realPctList.safeAverage()

        val avgWinAmount = winsListDollar.safeAverage()
        val avgLossAmount = lossesListDollar.safeAverage()
        val avgPctGain = winsListPercent.safeAverage()
        val avgPctLoss = lossesListPercent.safeAverage()
        val daysWins = winsListDollar.size
        val daysLosses = lossesListDollar.size
        val nPositionsClosed = closed.size

        val winLossRatio = if (nPositionsClosed != 0)
            daysWins.toDouble() / daysLosses.toDouble()
        else 0.0

        val winPercentage = if (nPositionsClosed != 0)
            daysWins.toDouble() / nPositionsClosed.toDouble() * 100.toDouble()
        else 0.0

        val daysBiggestGain = winsListDollar.maxOrNull() ?: 0.0
        val daysBiggestLoss = lossesListDollar.minOrNull() ?: 0.0
        val nPosOpenedToday = getNPosOpenedToday(daysPosList)
        val t = System.currentTimeMillis()

        val bs = _botState.value.copy(
            gainLossDollarDaysRealized = gDolDaysRealized,
            gainLossPercentDaysRealized = gPctRealized,
            gainLossDollarDaysTotal = gDolDaysTotal,
            gainLossDollarUnrealized = gDolDaysUnrealized,
            gainLossDollarDaysRealizedAvg = gDolAvg,
            gainLossPercentDaysRealizedAvg = gPctAvg,
            nOpenPositions = open.size,
            nClosedPositionsToday = closed.size,
            nTotalPositionsToday = open.size + closed.size,
            curCashPapertradeBalance = getCashBalance(),
            buyingPower = getBuyingPower(),
            curTotalBalance = getTotalBalance(),
            lastUpdateTimestampMs = t,
            lastUpdateDate = Date(t),
            curOpenPositions = open,
            daysClosedPositions = closed,
            avgWinAmount = avgWinAmount,
            avgLossAmount = avgLossAmount,
            avgPercentGain = avgPctGain,
            avgPercentLoss = avgPctLoss,
            daysWins = daysWins,
            daysLosses = daysLosses,
            daysWinLossRatio = winLossRatio,
            daysWinPercentage = winPercentage,
            daysBiggestGain = daysBiggestGain,
            daysBiggestLoss = daysBiggestLoss,
            nPositionsOpenedToday = nPosOpenedToday,
            daysTradedTicks = daysTradedTickers,
            topGainingTicker = topGainingTicker,
            topGainingTickerGainDollar = topGainingTickerAmount,
            daysTickerGainMap = daysTickerGainMap,
            cycleCount = cycleCount
        )

        saveBotState(bs)
    }


    fun getOpenPosList(): List<OptionPosition> {
        return _botState.value.curOpenPositions
    }
    
    
    fun getAllPosList(): List<OptionPosition> {
        return posDb.getAllOptionPositions()
    }
    
    
    fun getClosedPosList(): List<OptionPosition> {
        return posDb.getAllClosedOptionPositions()
    }


    fun getTodaysClosed(): List<OptionPosition> {
        return posDb.getAllPositionsOfDay().filter { it.closeTimestampMs != 0L }
    }


    protected fun getNumberOfOpenPositions(): Int {
        return _botState.value.curOpenPositions.size
    }


    protected fun getNumberOfOpenPositionsOnTicker(t: String): Int {
        val oList = posDb.getAllOpenOptionPositions()
        var n = 0
        for (p in oList){
            if (p.stockSymbol == t){
                n++
            }
        }
        return n
    }


    protected fun getNumberOfLossesOnTicker(t: String): Int {
        val closedPosList = getTodaysClosed()

        var losses = 0
        for (p in closedPosList){
            if (p.stockSymbol != t) continue

            if (p.gainLossDollarTotal < 0.0){
                losses += 1
            }
        }
        return losses
    }


    protected fun getTickerBlacklist(): List<String> {
        return _botState.value.tickerBlackList
    }


    /** Adds tickerSymbol to Blacklist in state. */
    protected fun addTickersToBlacklist(vararg tickerSymbols: String) {

        val bl = _botState.value.tickerBlackList.toMutableList()
        val added = mutableListOf<String>()
        for (t in tickerSymbols){
            bl.add(t)
            added.add(t)
        }

        val state = _botState.value.copy(tickerBlackList = bl, blackListLastUpdatedMs = System.currentTimeMillis())
        saveBotState(state)
        log.dbug("addTickerToBlacklist() Called. $added Added To Blacklist. Current Blacklist: $bl")
        log.info("addTickerToBlacklist() Called. $added Added To Blacklist. Current Blacklist: $bl")
    }


    /** Adds tickerSymbol to Watchlist in state. */
    protected fun addTickerToWatchlist(vararg tickerSymbols: String) {
        val wl = _botState.value.watchlist.toMutableList()
        val added = mutableListOf<String>()
        for (t in tickerSymbols) {
            wl.add(t)
            added.add(t)
        }
        val s = _botState.value.copy(
            watchlist = wl
        )
        saveBotState(s)
        log.dbug("addTickerToWatchlist() Called. $added Added to Watchlist. Current Watchlist: $wl")
    }


    protected fun replaceWatchlist(newWatchlist: List<String>) {
        saveBotState(_botState.value.copy(
            watchlist = newWatchlist,
            watchlistLastUpdateMs = System.currentTimeMillis()
        ))
        log.info("replaceWatchlist() New List: $newWatchlist")
    }


    fun openPosition(
        optionSymbol: String,
        quantity: Int,
        tpDollar: Double,
        tpPct: Double,
        stopDollar: Double,
        stopPct: Double,
        extraData: Map<String, String>
    ): Boolean {
        // Buying power is updated at the start of each cycle
        // and maintained

        val pos = orderManager.buyOrder(
            botName,
            botDesc,
            optionSymbol,
            quantity,
            tpDollar,
            tpPct,
            stopDollar,
            stopPct,
            extraData
        )
        if (pos == null){
            log.warn("openNewPosition(): Failed to open position" +
                    " for $optionSymbol, quan: $quantity.")
            return false
        }

        // Update balances + open pos list
        updateBot()

        // Log the purchase
        val price = pos.totalPrice
        val newBp = _botState.value.buyingPower
        val newBal = _botState.value.curCashPapertradeBalance
        val totalBal = _botState.value.curTotalBalance
        log.dbug("openPosition() Position Opened. $optionSymbol Quantity: $quantity " +
                "Total Cost: $price Cost Per: ${pos.pricePer} Buying Power Left: $newBp " +
                "New Cash Balance: $newBal New Total Balance: $totalBal")
        log.info("openPosition() Position Opened. $optionSymbol Quantity: $quantity " +
                "Total Cost: $price Cost Per: ${pos.pricePer} Buying Power Left: $newBp " +
                "New Cash Balance: $newBal New Total Balance: $totalBal")
        return true
    }


    fun closePosition(
        pos: OptionPosition,
        closeReason: String,
        extraClosingData: String = ""
    ): Boolean {

        val closed = orderManager.sellOrder(pos, closeReason, extraClosingData)

        if (closed == null){
            log.warn("closePosition(): Failed to close position" +
                    " for ${pos.optionSymbol}, quan: ${pos.quantity}.")
            return false
        }

        // Update balances + open pos list
        updateBot()
        val newBp = _botState.value.buyingPower
        val newBal = _botState.value.curCashPapertradeBalance
        val totalBal = _botState.value.curTotalBalance

        log.dbug("closePosition() Position Closed. ${pos.optionSymbol} Quantity: ${pos.quantity} " +
                "Close Reason: $closeReason Total Val: $${closed.curValueOfPosition} " +
                "Gain: $${pos.gainLossDollarTotal} - %${pos.gainLossPercent} " +
                "Gain Per: $${pos.gainLossDollarPer} New Total Balance: $totalBal")
        log.info("closePosition() Position Closed. ${pos.optionSymbol} Quantity: ${pos.quantity} " +
                "Close Reason: $closeReason Total Val: $${closed.curValueOfPosition} " +
                "Gain: $${pos.gainLossDollarTotal} - %${pos.gainLossPercent} " +
                "Gain Per: $${pos.gainLossDollarPer} New Total Balance: $totalBal")

        return true
    }


    /** Closes the position if the takeProfitDollarTarget or stopLossDollarTarget is reached.
     *
     * IMPORTANT: Goes by the 'curValuePerContract' of the OptionPosition. */
    protected fun checkOpenPositionsOptionTpStopDollarPer() {
        val openList = _botState.value.curOpenPositions
        for (p in openList){
            val tpDol = p.takeProfitDollarTarget
            val stopDol = p.stopLossDollarTarget
            val curValPer = p.curValuePerContract

            // Take Profit
            if (curValPer > tpDol){
                closePosition(p, "Take Profit Dollar target reached")
            }

            // Stop Loss
            else if (curValPer < stopDol){
                closePosition(p, "Stop Loss Dollar target reached")
            }

            // Logging is done in closePosition()
        }
    }


    /** Closes the position if the takeProfitPercentTarget or stopLossPercentTarget is reached
     *
     * IMPORTANT: Goes by the 'gainLossPercent' of the OptionPosition. */
    protected fun checkOpenPositionsOptionTpStopPercent() {
        val openList = _botState.value.curOpenPositions
        for (p in openList){
            val tp = p.takeProfitPercentTarget
            val stop = p.stopLossPercentTarget
            val curPctGainLoss = p.gainLossPercent

            // Take Profit
            if (curPctGainLoss > tp){
                closePosition(p, "Take Profit Dollar target reached")
            }

            // Stop Loss
            else if (curPctGainLoss < stop){
                closePosition(p, "Stop Loss Dollar target reached")
            }

            // Logging is done in closePosition()
        }
    }


    fun closeAllPositions(): Boolean {
        val posList = botState.value.curOpenPositions
        val failedClosures = mutableListOf<OptionPosition>()
        val deferredClosed = mutableListOf<Deferred<Unit>>()
        // Loop through and launch coroutines attempting to close each position
        for (p in posList){
            val asyncClose = coroutineScopeIO.async {
                val closed = closePosition(p, "close all positions called.")
                if (closed == false){
                    failedClosures.add(p)
                }
            }
            deferredClosed.add(asyncClose)
        }

        // Await the coroutines
        runBlocking {
            deferredClosed.awaitAll()
        }
        // Report the failures
        if (failedClosures.isNotEmpty()){
            Log.i("$botName.closeAllPositions()", "Failed to close OptionPositions: " +
                    "${failedClosures.map { it.optionSymbol }}")
            return false
        }
        return true
    }



    // Abstract Functions Start ///////////////////////////////////////////////////////////////////////////////////////


    abstract protected fun engine1TimeCheckIsOkToCycle(): Boolean

    /** Check the overall macroeconomic status of the day */
    abstract protected fun engine2MacroMarketOverview(): Boolean


    /** Build watchlist to scan */
    abstract protected fun engine3BuildWatchlist(): List<String>


    /** Primarily used for chart analysis */
    abstract protected fun engine4PrimaryAnalysis(watchlist: List<String>): List<AnalysisResult>


    /** Double check triggers from primary analysis */
    abstract protected fun engine5AnalysisFilter(analysisResults: List<AnalysisResult>): List<AnalysisResult>


    /** Enter open orders */
    abstract protected fun engine6BuyOrderEntry(analysisResults: List<AnalysisResult>)


    // TODO Consider changing the DB so that getOpenPositions returns a flow
    //      - that way the moment tp/stop triggers are reached the pos will be closed
    /** Check for sale triggers */
    abstract protected fun enginePositionMonitoring(pos: OptionPosition)


    /** Ran at end of each cycle */
    abstract protected fun engine7StatusUpdate()



    // Power/Thread Functions Start ////////////////////////////////////////////////////////////////////////////////////



    /** Turns bot ON and updates 'power' value in state to TRUE */
    fun powerOn() {
        power.set(true)
        val bs = _botState.value.copy(power = true)
        saveBotState(bs)
        log.dbug("powerOn() Ran()")
        log.info("powerOn()")
    }


    /** Turns bot OFF and updates 'power' value in state to FALSE */
    fun powerOff() {
        power.set(false)
        val bs = _botState.value.copy(power = false)
        saveBotState(bs)

        log.dbug("powerOff() Ran()")
        log.info("powerOff()")
    }


    /** Returns the power value */
    fun getPower(): Boolean{
        val p = power.get()
        log.dbug("getPower() Ran(). Cur Value: $p")
        return p
    }


//    fun shutDownBotThread() {
//        this.botThreadShutdown.set(true)
//    }


    protected fun getBotThreadShutdownValue(): Boolean {
        return botThreadShutdown.get()
    }


    private fun addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(
            Thread{
                log.warn("Shutdown Hook Triggered")
                this.botThreadShutdown.set(true)
                this.powerOff()
                coroutineScopePosUpdate.cancel()
            }
        )
    }


    private fun startPosBackgroundUpdatesAndMonitoring() {

        // Build List of cur pos symbols and add requests for thier quotes
        val openListSymbols = posDb.getAllOpenOptionPositions().map { it.optionSymbol }
        PosUpdateManager.addMultipleOptionSymbolsToUpdateRequests(openListSymbols)

        // Start monitoring quote flow
        PosUpdateManager.quoteMapStateflow.collectLatestSafe(
            this@BaseBot,
            scope = coroutineScopePosUpdate,
            ) { mapOfQuotes ->

            val openList = posDb.getAllOpenOptionPositions()

            for (p in openList){
                val quoteData = mapOfQuotes.get(p.optionSymbol) ?: continue
                val updated = PosUpdateManager.updateOptionPositionWithQuote(p, quoteData)
                if (updated == null){
                    log.warn("startPosUPdates() Failed to update pos with " +
                            "PosUpdateManager.updateOptionPositionWithQuote(). " +
                            "id: ${p.id}\n" +
                            "pos: $p\n" +
                            "quoteData: $quoteData")
                    continue
                }
                posDb.updateOptionPosition(updated)
                enginePositionMonitoring(updated)
            }

        }
    }


    private fun sleepThreadSafe(time: Long? = null){
        try {
            sleep(time ?: postCycleSleepTimeMs)
        }catch (e: Exception){
            log.warn("Sleep Interrupted. Exiting Thread")
        }
    }


    override fun run() {
        super.run()

//         Run Bot Start Up & Start Pos Monitoring Thread
        try {
            // Load botstate from db, update starting balance/buying power // Will only load once per program start
            initBot()

            // Start background thread for pos monitoring
            startPosBackgroundUpdatesAndMonitoring()


        } catch (e: Exception){

            log.warn("Failed with Exception to initialize Bot with 'initBot()' or 'runSubclassInit()' or Failed to run" +
                    " 'startPosMonitoringThresad()'\nException MSG: ${e.message}\nException ${e.stackTrace}")
            e.printStackTrace()



//             Exit the thread and return
            return
        }

        // Main Bot Loop
        while (!getBotThreadShutdownValue()) {

            try {
                updateBot()

                if (MarketTimeUtil.isMarketOpen() == false){
                    val waitTime = MarketTimeUtil.getMarketWaitTimeInMillis()
                    val hours = waitTime.toDouble() / 1000.0 / 60.0 / 60.0
                    log.warn("Market Is CLOSED! Wait time: ${doubleToTwoDecimalFormat(hours)} hours.")
                    sleepThreadSafe(waitTime)
                    continue
                }

                // Updates gain/loss, balances, statistics. Should be ran before other methods so they have fresh values
                cycleCount++

                // If power is off, the pos list and state will still update
                if (getPower() == false) {
                    if (getBotThreadShutdownValue()) return
                    sleepThreadSafe()
                    continue
                }

                val timeCheck = engine1TimeCheckIsOkToCycle()
                if (timeCheck == false){
                    if (getBotThreadShutdownValue()) return
                    sleepThreadSafe()
                    continue
                }

                // If power is off, the pos list and state will still update
                if (getPower() == false) {
                    if (getBotThreadShutdownValue()) return
                    sleepThreadSafe()
                    continue
                }

                // Macro market Check
                val macroMarketCheck = engine2MacroMarketOverview()
                if (macroMarketCheck == false) {
                    sleepThreadSafe()
                    continue
                }
                if (getPower() == false) {
                    if (getBotThreadShutdownValue()) return
                    sleepThreadSafe()
                    continue
                }

                // Watchlist Builder
                val watchlist = engine3BuildWatchlist()
                if (watchlist.isEmpty()){
                    sleepThreadSafe()
                    continue
                }
                if (getPower() == false) {
                    if (getBotThreadShutdownValue()) return
                    sleepThreadSafe()
                    continue
                }

                // Primary Analysis
                val analysisResult = engine4PrimaryAnalysis(watchlist)
                if (analysisResult.isEmpty()){
                    sleepThreadSafe()
                    continue
                }
                if (getPower() == false) {
                    if (getBotThreadShutdownValue()) return
                    sleepThreadSafe()
                    continue
                }

                // Analysis Filter
                val filteredAnalysisResults = engine5AnalysisFilter(analysisResult)
                if (filteredAnalysisResults.isEmpty()){
                    sleepThreadSafe()
                    continue
                }
                if (getPower() == false) {
                    if (getBotThreadShutdownValue()) return
                    sleepThreadSafe()
                    continue
                }


                // Buy Order
                engine6BuyOrderEntry(filteredAnalysisResults)

                // Subclass Status Update
                engine7StatusUpdate()

                // Update bot so app has fresh data
                updateBot()

                // Sleep
                sleepThreadSafe()
            } catch (e: Exception){

                log.warn("run() Main Bot Loop Failed with Exception. Will Retry After Sleep.\n" +
                        "Message: ${e.message}\nStackTrace: ${e.stackTrace}")
                e.printStackTrace()
                sleepThreadSafe()
            }

        }

    }


    override fun interrupt() {
        super.interrupt()
        powerOff()
    }

}