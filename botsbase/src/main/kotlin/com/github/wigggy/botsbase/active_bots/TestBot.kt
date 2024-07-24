package com.github.wigggy.botsbase.active_bots

import com.github.wigggy.botsbase.systems.BaseBot
import com.github.wigggy.botsbase.systems.bot_tools.Common
import com.github.wigggy.botsbase.systems.data.MarketData
import com.github.wigggy.botsbase.systems.data.data_objs.AnalysisResult
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import com.github.wigggy.botsbase.systems.data.data_objs.TradePermissionCheckResults
import kotlin.random.Random

class TestBot: BaseBot(
    botName = TestBot::class.java.simpleName.toString(),
    botDesc = "Bot created for test purposes",
    dateBotCreatedMMDDYYYY = "07052024",
    startingPapertradeBalance = 10_000.0,
    postCycleSleepTimeMs = 5000L
) {

    fun generateSamplePositions(nPosToGenerateEachCallAndPut: Int = 10) {
        val cs = Common.csApi

        val chain = cs.getOptionChain("SPY", weeksAhead = 2, range = "NTM")
        val expirys = chain?.callExpDateMap?.keys!!

        val losCalls = mutableListOf<String>()
        val losPuts = mutableListOf<String>()
        for (e in expirys){
            val sm = chain.callExpDateMap!!.get(e)!!.keys
            val psm = chain.putExpDateMap!!.get(e)!!.keys

            for (s in sm){
                val o = chain.callExpDateMap[e]!![s]!!.symbol
                val po = chain.putExpDateMap[e]!![s]!!.symbol
                losCalls.add(o)
                losPuts.add(po)
            }
        }

        val los = losCalls.take(nPosToGenerateEachCallAndPut) + losPuts.take(nPosToGenerateEachCallAndPut)

        for (s in los){
            val p = this.orderManager.buyOrder(
                this.botName, botState.value.botDesc, s, Random.nextInt(10),
                0.0, 0.0, 0.0, 0.0, mapOf()
            )
            if (p != null){
                println("Opened $s")
            }
        }

        println("${los.size} Positions Created... ${los}")
    }


    override fun engine2MacroMarketOverview(): Boolean {
        return true
    }

    override fun engine3BuildWatchlist(): List<String> {
        return MarketData.getTopOptionableTickers()?.map { it.first } ?: listOf()
    }

    override fun engine4PrimaryAnalysis(watchlist: List<String>): List<AnalysisResult> {
        TODO("Not yet implemented")
    }

    override fun engine5AnalysisFilter(analysisResults: List<AnalysisResult>): List<AnalysisResult> {
        TODO("Not yet implemented")
    }

    override fun engine6BuyOrderEntry(analysisResults: List<AnalysisResult>) {
        TODO("Not yet implemented")
    }

    override fun engine1TimeCheckIsOkToCycle(): Boolean {
        return true
    }

    override fun enginePositionMonitoring(pos: OptionPosition) {
        return
    }

    override fun engine7StatusUpdate() {
        TODO("Not yet implemented")
    }
}

fun main() {

//    'Jul 20, 2024'
//    val tb = TestBot()
//    tb.generateSamplePositions(5)

    val t = Common.csApi.getStockQuote("SPY")
    println(t)

}

