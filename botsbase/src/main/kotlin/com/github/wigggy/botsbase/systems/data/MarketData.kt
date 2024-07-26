package com.github.wigggy.botsbase.systems.data

import com.github.wigggy.botsbase.systems.bot_tools.ColorLogger
import com.github.wigggy.botsbase.systems.bot_tools.Common
import com.github.wigggy.botsbase.tools.Log
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object MarketData {

    private val log = ColorLogger("MarketData")
    val csApi = Common.csApi

    // Cached data
    private var top25OptionableTickers = listOf<Pair<String, Int>>()
    private var lastTimeTop25OptionablTickersScanned = 0L
    private val timeoutFortop25OptionableTickers = 7_200_000     // 2hour


    // TODO RE-write these --
    private fun <T> threadPoolHandler(tasks: List<Callable<T>> ,timeoutMS: Long = 7000): List<T> {
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val data = mutableListOf<T>()
        try {
            val futures = mutableListOf<Future<T>>()

            // Start tasks
            try {
                for (t in tasks){

                    futures.add(executor.submit(t))
                }
            } catch (e: Exception) {
                Log.w("threadHandler()", "Task Failed with exception: ${e.message}")
            }

            // Collect results
            try {
                for (f in futures) {
                    val d = f.get(timeoutMS, TimeUnit.MILLISECONDS)
                    data.add(d)
                }
            } catch (e: Exception) {
                Log.w("threadHandler()", "Task Failed to return result: ${e.message}")
            }

        } finally {
            executor.shutdown()
        }
        return data
    }

    @Synchronized       // No need for multiple synchronous calls to this because of the Threadpool use
    fun getTopOptionableTickers(): List<Pair<String, Int>>? {

        // Return cached data if available and if not timedout
        if (top25OptionableTickers.isNotEmpty()){
            val curtime = System.currentTimeMillis()
            val timeout = lastTimeTop25OptionablTickersScanned + timeoutFortop25OptionableTickers
            if (curtime < timeout){
                return top25OptionableTickers
            }
        }

        // Use thread pool (5 limit) to build list of top 25 optionable stocks
        try {
            val topStockLists = csApi.getTopStocks()
            val targets = (topStockLists.etfTop25 + topStockLists.sp100 + topStockLists.nasdaq100).toSet()
            val failed = mutableListOf<String>()
            val callableTaskList = mutableListOf<Callable<Pair<String, Int>?>>()
            // Build tasks
            for (t in targets) {
                val callable = Callable {
                    try {
                        val chainResp = csApi.getOptionChain(t, weeksAhead = 4)
                        if (chainResp == null) {
                            failed.add(t)
                            println(t + " FAILED. NULL RESPONSE")
                            return@Callable Pair(t, 0)
                        }
                        var totalVol = 0
                        val expDate = chainResp!!.callExpDateMap.keys.first()
                        val callStrikeMap = chainResp.callExpDateMap[expDate]!!
                        val putStrikeMap = chainResp.putExpDateMap[expDate]!!
                        for (s in callStrikeMap!!.keys) {
                            totalVol += callStrikeMap[s]!!.totalVolume + putStrikeMap[s]!!.totalVolume
                        }
                        return@Callable Pair(t, totalVol)

                    } catch (e: Exception) {
                        println("$t Failed")
                        return@Callable Pair(t, 0)
                    }
                }
                callableTaskList.add(callable)
            }

            val results = threadPoolHandler(callableTaskList)
                .filterNotNull()
                .sortedByDescending { it.second }
                .take(25)

            top25OptionableTickers = results
            lastTimeTop25OptionablTickersScanned = System.currentTimeMillis()
            return results
        } catch (e: Exception) {
            Log.w("getTopOptionVolumeTickers()", "Failed Response: ${e.message}")
            return null
        }
    }

    private fun getOptionSym(ticker: String, typeCorP: String, itmDepth: Int = 0, minDte: Int = 1): String? {

        // Get ticker quote to find target strike
        val q = csApi.getStockQuote(ticker) ?: return null
        val callItmStrike = q.lastPrice.toString().split(".")[0].toInt()
        val putItmStrike = (callItmStrike.toInt() + 1)
        val targetStrike = if (typeCorP == "C"){
            (callItmStrike - itmDepth).toDouble()
        } else {
            (putItmStrike + itmDepth).toDouble()
        }

        // Get chain with the ammount of weeks to get matching minDte
        val weeks = minDte / 7
        val weeksToGet = if (weeks == 0) 2 else weeks * 2
        val chain = csApi.getOptionChain(ticker, strikeCount = -1, range = "ALL", weeksAhead = weeksToGet) ?: return null

        // Find expiry with acceptable minDte
        val expirys = chain.callExpDateMap.keys
        var expiry = ""
        for (e in expirys){
            val dte = e.split(":")[1]
            if (dte.toInt() >= minDte){
                expiry = e
                break
            }
        }

        // Try to find matching target strike w/o looping
        val s = chain.callExpDateMap[expiry]!![targetStrike.toString()]
        if (s != null){
            if (typeCorP == "C"){
                return chain.putExpDateMap[expiry]!![targetStrike.toString()]!!.symbol
            }else {
                return chain.callExpDateMap[expiry]!![targetStrike.toString()]!!.symbol
            }
        }

        // If exact match not found, loop through and find closest strike to target strike
        val sMap = chain.callExpDateMap[expiry]!!.keys
        var strike = "0.0"
        var smallestDiff = 9999.0
        for (stringStrike in sMap){
            val str = stringStrike.toDouble()
            val d = abs(targetStrike - str)
            if (smallestDiff == 9999.0){
                strike = stringStrike
                smallestDiff = d
                continue
            }

            if (d < smallestDiff){
                strike = stringStrike
                smallestDiff = d
            }
        }

        if (typeCorP == "C"){
            return chain.callExpDateMap[expiry]!![strike]!!.symbol
        }else {
            return chain.putExpDateMap[expiry]!![strike]!!.symbol
        }
    }

    /** Returns Call symbol matching requirements.
     *
     * @param itmDepth: 0 returns ATM, + Values return ITM, - Values return OTM
     * @param minDte Minimum amount of DTE
     * */
    fun optionSymbolSearchCall(ticker: String, itmDepth: Int, minDte: Int): String? {
        return getOptionSym(ticker, "C", itmDepth = itmDepth, minDte = minDte)
    }

    /** Returns Put symbol matching requirements.
     *
     * @param itmDepth 0 returns ATM, + Values return ITM, - Values return OTM
     * @param minDte Minimum amount of DTE
     * */
    fun optionSymbolSearchPut(ticker: String, itmDepth: Int, minDte: Int): String? {
        return getOptionSym(ticker, "P", itmDepth, minDte)
    }

}

fun main() {
    val x = MarketData.getTopOptionableTickers()
    println(x)
}