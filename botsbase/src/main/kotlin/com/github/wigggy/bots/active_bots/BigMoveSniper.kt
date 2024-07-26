package com.github.wigggy.bots.active_bots

import com.github.wigggy.botsbase.systems.BaseBot
import com.github.wigggy.botsbase.systems.bot_tools.MarketTimeUtil
import com.github.wigggy.botsbase.systems.data.MarketData
import com.github.wigggy.botsbase.systems.data.data_objs.AnalysisResult
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import kotlin.math.absoluteValue

class BigMoveSniper: BaseBot(
    "BigMoveSniper",
    "Finds stocks that have made Large notable moves and opens positions on them in favor of the momentum.",
    "07222024", 5000.0, 5000L
) {

    private val maxOpenPos = 2
    private val maxOpenPerTicker = 1
    private val minPctAbsValMoveAmount = 3.0
    private val tpPctTarget = 15.0
    private val stopPctTarget = -25.0
    private val startTimeHour = 9
    private val startTimeMin = 45
    private val endTimeHour = 15
    private val endTimeMin = 30
    private val maxEODCloseOutAttempts = 10
    private val minPriceOfContracts = 50.0

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


    // NOTE This method stops the flow from having more than 1 open pos at a time.
    // NOTE This method also performs time check to see if start time is reached
    override fun engine3BuildWatchlist(): List<String> {

        // Max Positions check
        if (getNumberOfOpenPositions() >= maxOpenPos){
            return listOf()
        }

        // Get Top Optionable Stocks
        val topTickers = MarketData.getTopOptionableTickers()?.map { it.first } ?: return listOf()

        val filteredList = topTickers.filter { s ->
            getNumberOfLossesOnTicker(s) == 0 && getTickerBlacklist().contains(s) == false
        }
        replaceWatchlist(filteredList)
        return filteredList
    }


    override fun engine4PrimaryAnalysis(watchlist: List<String>): List<AnalysisResult> {
        val quotes = csApi.getMultiStockQuote(watchlist) ?: return listOf()

        // Loop through quotes and find the biggest mover
        var biggestMover: String = ""
        var moveAmtPct = 0.0
        var putOrCall = ""      // C or P
        for (k in quotes.keys){
            val quote = quotes[k] ?: continue
            val movePct = quote.netPercentChange
            if (movePct.absoluteValue > moveAmtPct.absoluteValue){
                biggestMover = quote.symbol
                moveAmtPct = movePct.absoluteValue
                putOrCall = if (movePct > 0.0) "C" else "P"
            }
        }

        if (moveAmtPct.absoluteValue < minPctAbsValMoveAmount.absoluteValue){
            return listOf()
        }

        if (biggestMover != ""){
            val analysisResult = AnalysisResult(
                ticker = biggestMover,
                triggerValue = if (putOrCall == "C") 1 else -1,

                // Not using these, this class has standard tp/stop pct values
                0.0,
                0.0,
                0.0,
                0.0
            )
            return listOf(analysisResult)
        }
        return listOf()
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
        }
    }


    override fun engine7StatusUpdate() {

        // Check for eod closeout
        if (MarketTimeUtil.isTimeAfter(15, 30)) {     // 3:30pm

            // Loop until pos list is clear
            while (true){
                val pList = getOpenPosList()

                // Success flag starts true, if a single pos fails to close, will be set to false
                var allCloseSuccess = true

                // Loop and try to close each pos, if fail, set flag to false
                for (p in pList){
                    val closeSuccess = closePosition(p, "EOD CLOSE OUT")
                    if (!closeSuccess){
                        allCloseSuccess = false
                    }
                }

                // If fail, sleep 5s
                if (allCloseSuccess == false){
                    Thread.sleep(5000L)
                } else {
                    break
                }
            }
        }

        return
    }


}

fun main() {

    val s = "F     240726C00012000"

    val bot = BigMoveSniper()
    val l = bot.getOpenPosList()
    println(l)
    bot.closeAllPositions()


}


