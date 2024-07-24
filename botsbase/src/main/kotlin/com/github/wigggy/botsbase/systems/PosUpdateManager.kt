package com.github.wigggy.botsbase.systems

import com.github.wi110r.charlesschwab_api.data_objs.OptionQuote
import com.github.wigggy.botsbase.systems.bot_tools.BotToolsLogger
import com.github.wigggy.botsbase.systems.bot_tools.Common
import com.github.wigggy.botsbase.systems.bot_tools.MarketTimeUtil
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import com.github.wigggy.botsbase.tools.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

object PosUpdateManager {

    private val log = BotToolsLogger("PosUpdateManager")
    private val csApi = Common.csApi

    private val updateRequestList = mutableListOf<String>()
    private val _quoteMapStateFlow = MutableStateFlow<Map<String, OptionQuote>>(mapOf())
    val quoteMapStateflow = _quoteMapStateFlow.asStateFlow()

    private val updateCycleTime = 5_000L        // 6seconds
    private val marketClosedCycleTimeMultiplier = 10L       // If closed sleep time will be cycleTime X 10
    private var updateThread: Thread? = null
    private val updateThreadShutdownFlag = AtomicBoolean(false)


    init {
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(
            Thread{
                shutdownUpdaterThread()
                log.w("init() Shutdown Hook Triggered. Shutting down updateThread")
            }
        )
    }


    fun addOptionSymbolToUpdateRequests(s: String){
        val contains = updateRequestList.contains(s)
        if (contains == false){
            updateRequestList.add(s.uppercase())
        }
    }

    fun addMultipleOptionSymbolsToUpdateRequests(listOfSyms: List<String>){
        for (s in listOfSyms){
            addOptionSymbolToUpdateRequests(s)
        }
    }


    fun removeOptionSymbolFromUpdateRequests(s: String){
        updateRequestList.remove(s.uppercase())
    }


    /** Fetches Quotes and Updates _quoteMapStateFlow. */
    private fun fetchQuotesAndUpdateStateFlow() {
        try {
            if (updateRequestList.isEmpty()) return

            val quotes = csApi.getMultiOptionQuote(updateRequestList)

            if (quotes == null){
                log.w("fetchQuotes() Failed to get quotes for ${_quoteMapStateFlow.value.keys.size} symbols. " +
                        "Null return.")
                return
            }

            _quoteMapStateFlow.value = quotes
        } catch (e: Exception){
            log.w("fetchQuotes() Failed to get quotes for ${_quoteMapStateFlow.value.keys.size} symbols. " +
                    "Exception Triggered. Message: ${e.message}")
            e.printStackTrace()
        }
    }


    fun startUpdaterThread() {

        updateThread = Thread {
            while (updateThreadShutdownFlag.get() == false){

                // Fetch update, track time taken
                val cycleStart = System.currentTimeMillis()
                fetchQuotesAndUpdateStateFlow()
                val end = System.currentTimeMillis()

                // Calculate sleep time so next update happens exactly on time if market is open, if not increase time
                val sleepTime = if (MarketTimeUtil.isMarketOpen()){
                    updateCycleTime - (end - cycleStart)
                } else {
                    updateCycleTime * marketClosedCycleTimeMultiplier
                }
                if (sleepTime <= 0) continue
                try { Thread.sleep(sleepTime) }
                catch (e: Exception) { log.w("updateThread Thread.sleep() Interrupted"); break}
            }
            log.w("startUpdaterThread() updateThreadShutdownFlag set to true. updateThread is Dead")
        }

        updateThread!!.start()
    }


    // Not needed
    fun shutdownUpdaterThread() {
        updateThread?.interrupt()
        updateThreadShutdownFlag.set(true)
    }


    fun updateOptionPositionWithQuote(pos: OptionPosition, q: OptionQuote): OptionPosition? {
        try {
            val time = System.currentTimeMillis()
            val curTotalValue = q.bidPrice * pos.quantity.toDouble() - pos.fees
            val gainDollarTotal = curTotalValue - pos.totalPrice
            val gainDollarPer = q.bidPrice - pos.pricePer
            val gainPercent = (((q.bidPrice - .65) - pos.pricePer) / pos.pricePer) * 100

            val highGdPerCon =
                if (gainDollarPer > pos.highestGainDollarPerContract) gainDollarPer
                else pos.highestGainDollarPerContract
            val highGdTotal =
                if (gainDollarTotal > pos.highestGainDollarTotal) gainDollarTotal
                else pos.highestGainDollarTotal

            val lowestGdPerCon =
                if (gainDollarPer < pos.lowestGainDollarPerContract) gainDollarPer
                else pos.lowestGainDollarPerContract
            val lowestGdTotal =
                if (gainDollarTotal < pos.lowestGainDollarPerTotal) gainDollarTotal
                else pos.lowestGainDollarPerTotal

            val highestGPct =
                if (gainPercent > pos.highestGainPercent) gainPercent
                else pos.highestGainPercent
            val lowGPct =
                if (gainPercent < pos.lowestGainPercent) gainPercent
                else pos.lowestGainPercent

            val updatedPosition = pos.copy(
                lastUpdatedTimestampMs = time,
                lastUpdatedDate = java.util.Date(time),
                bid = q.bidPrice,
                ask = q.askPrice,
                mark = q.mark,
                highPrice = q.highPrice,
                lowPrice = q.lowPrice,
                openPrice = q.openPrice,
                totalVolume = q.totalVolume,
                volatility = q.volatility,
                delta = q.delta,
                gamma = q.gamma,
                theta = q.theta,
                vega = q.vega,
                rho = q.rho,
                openInterest = q.openInterest,
                timeValue = q.timeValue,
                theoreticalOptionValue = q.theoreticalOptionValue,
                dte = q.daysToExpiration,
                intrinsicValue = q.moneyIntrinsicValue,
                high52Week = q.weekHigh52,
                low52Week = q.weekLow52,
                inTheMoney = itmCheck(pos.putCall, pos.strikePrice, q.underlyingPrice),
                itmDistance = itmDistanceCheck(pos.putCall, pos.strikePrice, q.underlyingPrice),

                gainLossDollarTotal = gainDollarTotal,
                gainLossDollarPer = gainDollarPer,
                gainLossPercent = gainPercent,

                underlyingPriceCurrent = q.underlyingPrice,

                curValuePerContract = q.bidPrice - .65,
                curValueOfPosition = (q.bidPrice * pos.quantity.toDouble()) - (1.3 * pos.quantity.toDouble()),

                highestGainDollarPerContract = highGdPerCon,
                highestGainDollarTotal = highGdTotal,
                lowestGainDollarPerContract = lowestGdPerCon,
                lowestGainDollarPerTotal = lowestGdTotal,
                highestGainPercent = highestGPct,
                lowestGainPercent = lowGPct
            )
            return updatedPosition
        } catch (e: Exception) {
            Log.i(
                "PosUpdateManagerupdateOptionPositionWithQuote()", "Failed to update position ID: ${pos.id} " +
                        "SYM: ${pos.optionSymbol}",
                e.stackTraceToString()
            )
            return null
        }
    }


    private fun itmCheck(putCall: String, strike: Double, curStockValue: Double): Boolean {
        if (putCall == "CALL") {
            return if (strike > curStockValue) false else true
        } else {
            return if (strike > curStockValue) true else false
        }
    }


    private fun itmDistanceCheck(putCall: String, strike: Double, curStockValue: Double): Double {
        if (putCall == "CALL") {
            // cp - st
            return curStockValue - strike
        } else {
            // st - cp
            return strike - curStockValue
        }
    }

}