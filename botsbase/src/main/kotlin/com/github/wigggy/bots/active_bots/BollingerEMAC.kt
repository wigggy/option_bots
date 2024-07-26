package com.github.wigggy.bots.active_bots

import com.github.wigggy.botsbase.systems.BaseBot
import com.github.wigggy.botsbase.systems.analysis.TechnicalAnalysis
import com.github.wigggy.botsbase.systems.bot_tools.MarketTimeUtil
import com.github.wigggy.botsbase.systems.data.MarketData
import com.github.wigggy.botsbase.systems.data.data_objs.AnalysisResult
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import com.github.wigggy.charles_schwab_api.data_objs.stockchart.CharlesSchwabHistoricData
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class BollingerEMAC: BaseBot(
    "BollingerEMAC",
    "Uses Bollinger Bands combined with EMAC to detect Mean Reversion",
    "07262024",
    5000.0,
    30_000L
) {


    private val maxOpenPos = 2
    private val maxOpenPerTicker = 1
    private val tpPctTarget = 10.0
    private val stopPctTarget = -10.0
    private val startTimeHour = 9
    private val startTimeMin = 45
    private val endTimeHour = 15
    private val endTimeMin = 30
    private val maxEODCloseOutAttempts = 10
    private val minPriceOfContracts = 50.0

    // Analysis Parameters
    private val bollBreakoutWindow = 12
    private val emacWindow = 10


    override fun engine1TimeCheckIsOkToCycle(): Boolean {
        // Start Time check
        val startTimeOk = MarketTimeUtil.isTimeAfter(startTimeHour, startTimeMin)
        val endTimeOk = MarketTimeUtil.isTimeBefore(endTimeHour, endTimeMin)

        // End of day close all
        if (!endTimeOk){
            var attempts = 0
            while (attempts <= maxEODCloseOutAttempts){
                attempts++
                val success = closeAllPositions()
                if (success){
                    break
                }
            }
        }

        if (startTimeOk && endTimeOk){
            return true
        }else {
            return false
        }
    }

    override fun engine2MacroMarketOverview(): Boolean {
        return true
    }

    override fun engine3BuildWatchlist(): List<String> {
        // Max Positions check
        if (getNumberOfOpenPositions() >= maxOpenPos){
            return listOf()
        }

        // Get Top Optionable Stocks
        val topTickers = MarketData.getTopOptionableTickers()?.map { it.first } ?: return listOf()

        val filteredList = topTickers.filter { s ->
            getNumberOfLossesOnTicker(s) == 0
                    && getTickerBlacklist().contains(s) == false
                    && getNumberOfOpenPositionsOnTicker(s) < maxOpenPerTicker
        }

        replaceWatchlist(filteredList)
        return filteredList
    }

    override fun engine4PrimaryAnalysis(watchlist: List<String>): List<AnalysisResult> {

        val arList = mutableListOf<AnalysisResult>()

        val chartMap = getCharts(watchlist)

        for (sym in chartMap.keys){
            val chart = chartMap[sym] ?: continue

            val trigger = findEntryTrigger(chart)

            if (trigger == 1 || trigger == -1){
                val a = AnalysisResult(
                    sym, trigger,
                    0.0, 0.0, 0.0, 0.0
                )
                arList.add(a)
            }
        }

        return arList
    }

    override fun engine5AnalysisFilter(analysisResults: List<AnalysisResult>): List<AnalysisResult> {
        return analysisResults
    }

    override fun engine6BuyOrderEntry(analysisResults: List<AnalysisResult>) {
        if (getNumberOfOpenPositions() >= maxOpenPos){
            return
        }

        for (a in analysisResults){

            // Get n of open pos for ticker, if at max continue
            val t = a.ticker
            val curNPos = getNumberOfOpenPositionsOnTicker(t)
            if (curNPos >= maxOpenPerTicker){
                continue
            }

            // Find contract
            val max = 5
            var attempts = 0
            var os: String? = null
            while (attempts <= max){
                if (a.triggerValue == 1){
                    os = MarketData.optionSymbolSearchCall(a.ticker, 0, 1)
                } else {
                    os = MarketData.optionSymbolSearchPut(a.ticker, 0, 1)
                }

                if (os != null){
                    break
                }else {
                    attempts ++
                    Thread.sleep(500)
                }
            }
            if (os == null){
                return
            }

            // Get Option quote for price of contract
            val q = csApi.getOptionQuote(os) ?: continue
            if (q.askPrice <= minPriceOfContracts){
                addTickersToBlacklist(t)
                continue
            }


            openPosition(os!!, 1, 0.0, tpPctTarget, 0.0, stopPctTarget, mapOf())
        }
    }

    override fun enginePositionMonitoring(p: OptionPosition) {
        val curGainPct = p.gainLossPercent
        if (curGainPct >= tpPctTarget){
            closePosition(p, "TP OF %$tpPctTarget REACHED", "")
        }
        else if (curGainPct <= stopPctTarget){
            closePosition(p, "STOP OF %$stopPctTarget REACHED", "")
            addTickersToBlacklist(p.stockSymbol)
        }    }

    override fun engine7StatusUpdate() {
        return
    }

    fun getCharts(wl: List<String>): Map<String, CharlesSchwabHistoricData?> {
        val vtp = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
        val crScope = CoroutineScope(vtp)
        val mapOfResults = mutableMapOf<String, Deferred<CharlesSchwabHistoricData?>>()
        for (s in wl){
            val asyncChart = crScope.async {
                return@async csApi.getHistoricData5min(s, 5, true)
            }
            mapOfResults[s] = asyncChart
        }

        val data = runBlocking {
            val results = mapOfResults.mapValues { (k, v) -> v.await() }
            return@runBlocking results
        }
        vtp.close()
        return data
    }

    fun findEntryTrigger(chart: CharlesSchwabHistoricData): Int{
        val bollbands = TechnicalAnalysis.bollingBandsSmaMiddleBand(chart.close, 20, 1.5)
        val highs = chart.high
        val lows = chart.low

        // Look for breakout
        var lowBreakout = false
        var highBreakout = false
        for (n in 1..bollBreakoutWindow){
            val indLoc = bollbands.lowerBand.lastIndex - n

            // Look for + breakout
            val lowerBandVal = bollbands.lowerBand[indLoc]
            val lowVal = lows[indLoc]
            if (lowVal < lowerBandVal){
                lowBreakout = true
                break
            }

            // Look for - breakout
            val upperBandVal = bollbands.upperBand[indLoc]
            val highVal = highs[indLoc]
            if (highVal > upperBandVal){
                highBreakout = true
            }
        }

        // Look for Bull Signal
        if (lowBreakout){
            val fast = TechnicalAnalysis.ema(highs, 6).emaValues
            val slow = TechnicalAnalysis.ema(highs, 12).emaValues

            for (n in 1..emacWindow){
                val indLoc = fast.lastIndex - n

                // 2 vals previous
                val prevPrevFVal = fast[indLoc - 2]
                val prevPrevSVal = slow[indLoc - 2]

                // Cur vals
                val fVal = fast[indLoc]
                val sVal = slow[indLoc]

                // Check for crossover were 2 candles ago NO cross and Cur candles are crossed
                if (prevPrevFVal < prevPrevSVal && fVal > sVal) {
                    return 1        // Bull Signal
                }

            }
        }
        // Look for Bear Signal
        else if (highBreakout) {
            val fast = TechnicalAnalysis.ema(lows, 6).emaValues
            val slow = TechnicalAnalysis.ema(lows, 12).emaValues

            for (n in 1..emacWindow){
                val indLoc = fast.lastIndex - n

                // 2 vals previous
                val prevPrevFVal = fast[indLoc - 2]
                val prevPrevSVal = slow[indLoc - 2]

                // Cur vals
                val fVal = fast[indLoc]
                val sVal = slow[indLoc]

                // Check for crossover were 2 candles ago NO cross and Cur candles are crossed
                if (prevPrevFVal > prevPrevSVal && fVal < sVal) {
                    return -1       // Bear signal
                }

            }
        }

        return 0    // No signal
    }

}



























