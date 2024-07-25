package com.github.wigggy.botsbase.systems.analysis.stock_chart

import com.github.wigggy.botsbase.systems.bot_tools.Common
import com.github.wigggy.charles_schwab_api.data_objs.stockchart.CharlesSchwabHistoricData
import java.util.*
import kotlin.math.abs



// TODO Go through and set property values for the following types of values
//      - closedAtHigh = .20, - isBottomHammer = .4
data class StockChartAdvanced(
    val ticker: String,
    val candleSize: String,
    val periodSize: Int,
    val prepost: Boolean,
    val datetime: List<Date>,
    val timestamp: List<Long>,
    val open: List<Double>,
    val high: List<Double>,
    val low: List<Double>,
    val close: List<Double>,
    val volume: List<Int>,
    val candles: List<Candle>
) {

    val lastIndex = open.lastIndex

    val size = open.size

    fun getOpenAtIndex(i: Int): Double {
        return open[findTrueIndex(i)]
    }

    fun getSublistOfOpen(startIndex: Int, endIndex: Int): List<Double> {
        return open.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getHighAtIndex(i: Int): Double {
        return high[findTrueIndex(i)]
    }

    fun getSublistOfHigh(startIndex: Int, endIndex: Int): List<Double> {
        return high.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getLowAtIndex(i: Int): Double {
        return low[findTrueIndex(i)]
    }

    fun getSublistOfLow(startIndex: Int, endIndex: Int): List<Double> {
        return low.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getCloseAtIndex(i: Int): Double {
        return close[findTrueIndex(i)]
    }

    fun getSublistOfClose(startIndex: Int, endIndex: Int): List<Double> {
        return close.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getVolumeAtIndex(i: Int): Int {
        return volume[findTrueIndex(i)]
    }

    fun getSublistOfVolume(startIndex: Int, endIndex: Int): List<Int> {
        return volume.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getDatetimeAtIndex(i: Int): Date {
        return datetime[findTrueIndex(i)]
    }

    fun getSublistOfDate(startIndex: Int, endIndex: Int): List<Date> {
        return datetime.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    fun getTimestampAtIndex(i: Int): Long {
        return timestamp[findTrueIndex(i)]
    }

    fun getSublistOfTimestamp(startIndex: Int, endIndex: Int): List<Long> {
        return timestamp.subList(findTrueIndex(startIndex), findTrueIndex(endIndex) + 1)
    }

    /**
     * Returns [Candle] object for given index which includes the values of...
     *
     * - datetime
     * - timestamp
     * - open
     * - high
     * - low
     * - close
     * - volume
     */
    fun getCandleAtIndex(i: Int): Candle {
        val trueIndex = findTrueIndex(i)

        return Candle(
            datetime = datetime[trueIndex],
            timestamp = timestamp[trueIndex],
            open = open[trueIndex],
            high = high[trueIndex],
            low = low[trueIndex],
            close = close[trueIndex],
            volume = volume[trueIndex]
        )
    }

    fun getSublistOfCandles(startIndex: Int, endIndex: Int): List<Candle> {
        val candles = mutableListOf<Candle>()
        for (n in findTrueIndex(startIndex)..findTrueIndex(endIndex)) {
            candles.add(getCandleAtIndex(n))
        }

        return candles
    }

    fun getSublistOfCandlesByDatetime(start: Date, end: Date): List<Candle> {

        val targetData = mutableListOf<Candle>()

        // Get list of all candles
        val allCandles = getSublistOfCandles(0, -1)

        // Check date of each candle, add to dataset if in range.
        for (c in allCandles) {

            // Break loop if endpoint has been reached
            if (c.datetime.after(end)){
                break
            }
            else if (c.datetime.after(start)){
                targetData.add(c)
            }

        }

        return targetData
    }

    /**
     * Used to determine index even if a negative value is provided.
     *
     * Example: -1 = last index. -2 = last index -1...(it works the same as Python indexing)
     */
    private fun findTrueIndex(i: Int): Int {
        if (i < 0){
            val target = lastIndex + i + 1
            return target
        } else {
            return i
        }
    }

    /**
     * Calculates the size of candle from High to Low for given index.
     */
    fun getCandleHighToLowMeasurement(index: Int): Double {
        val c = getCandleAtIndex(index)
        return c.high - c.low
    }


    /**
     * Calculates the size of candle's body for given index using abs(candle.open - candle.close)
     */
    fun getCandleBodyMeasurement(index: Int): Double {
        val c = getCandleAtIndex(index)
        return abs(c.open - c.close)
    }


    /**
     * Determines the "Color" of candle.
     *
     * - Returns...
     *      - "G" : open > close
     *      - "R" : close > open
     *      - "B" : close == open
     *
     */
    fun getCandleColorAsGorRorB(index: Int): String {

        val c = getCandleAtIndex(index)

        return if (c.close > c.open) {
            "G"
        } else if (c.close < c.open) {
            "R"
        } else {
            "B"
        }
    }


    /**
     * Calculates the Average Candle (High - Low) for the specified window ending
     * at the given index.
     */
    fun getAvgCandleHighToLow(index: Int, window: Int): Double {

        // Determine actual END index (even a negative value)
        val trueEndIndex = findTrueIndex(index)

        // Determine start index
        var startIndex = trueEndIndex - window
        var trueWindow = window
        if (startIndex < 0) {
            startIndex = 0
            trueWindow = trueEndIndex + 1
        }
        // Calculate avg for window
        var total = 0.0
        for (n in startIndex..trueEndIndex) {
            total += getCandleHighToLowMeasurement(n)
        }

        return total / trueWindow
    }


    /**
     * Calculates the Average Candle (abs(open - close)) for the specified window ending
     * at the given index
     */
    fun getAvgCandleBody(index: Int, window: Int): Double {
        // Find true end Index even with negative index value
        val trueEndIndex = findTrueIndex(index)

        // Determine start index / true window...aka check for < 0 start index
        var startIndex = trueEndIndex - window
        var trueWindow = window
        if (startIndex < 0) {
            startIndex = 0
            trueWindow = trueEndIndex + 1
        }

        // Calculate avg for window
        var total = 0.0
        for (n in startIndex..trueEndIndex) {
            total += getCandleBodyMeasurement(n)
        }
        return total / trueWindow
    }


    /**
     * Returns True if green candle closed within 10% of it's high
     */
    fun closedAtHigh(index: Int): Boolean {

        val c = getCandleAtIndex(index)

        // Make sure it's a green candle
        if (c.close < c.open) {
            return false
        }

        return getHeadSizeAsPercentageOfTotalSize(index) < .05
    }


    /**
     * Returns True if red candle closed within 10% of it's low
     */
    fun closedAtLow(index: Int): Boolean {

        val c = getCandleAtIndex(index)

        // Make sure candle is red
        if (c.close > c.open) {
            return false
        }

        return getTailSizeAsPercentageOfTotalSize(index) < .05
    }


    /**
     * Returns the Head size as a percentage of total size
     */
    fun getHeadSizeAsPercentageOfTotalSize(index: Int): Double {
        val c = getCandleAtIndex(index)

        return if (c.close > c.open) {

            (c.high - c.close) / (c.high - c.low)
        } else {
            (c.high - c.open) / (c.high - c.low)
        }

    }


    /**
     * Returns the Tail size as percentage of total size
     */
    fun getTailSizeAsPercentageOfTotalSize(index: Int): Double {
        val c = getCandleAtIndex(index)

        return if (c.close < c.open) {
            (c.close - c.low) / (c.high - c.low)
        } else {
            (c.open - c.low) / (c.high - c.low)
        }
    }


    /**
     * Returns the Body size as percentage of total size
     */
    fun getBodySizeAsPercentageOfTotalSize(index: Int): Double {
        val c = getCandleAtIndex(index)

        return abs(c.close - c.open) / (c.high - c.low)
    }


    /**
     * Returns the Percentage Difference of current body size vrs avg body size
     */
    fun getBodySizeVrsAvgBodyAsPercentage(index: Int, window: Int): Double {
        val targetCandle = getCandleBodyMeasurement(index)
        val avgCandle = getAvgCandleBody(index, window)

        return targetCandle / avgCandle
    }


    /**
     * Returns the Percentage Difference of current High to Low size vrs avg High to Low
     */
    fun getHighToLowSizeVrsAvgHighToLowAsPercentage(index: Int, window: Int): Double {
        val targetCandle = getCandleHighToLowMeasurement(index)
        val avgHighToLow = getAvgCandleHighToLow(index, window)

        return targetCandle / avgHighToLow
    }


    /**
     * Returns true if candle is Green
     */
    fun isGreenCandle(index: Int): Boolean {

        return when (getCandleColorAsGorRorB(index)) {
            "G" -> {
                true
            }

            else -> {
                false
            }
        }
    }


    /**
     * Returns true if candle is Red
     */
    fun isRedCandle(index: Int): Boolean {
        return when (getCandleColorAsGorRorB(index)) {
            "R" -> {
                true
            }

            else -> {
                false
            }
        }
    }


    /**
     * Checks if candle is above average body size
     */
    fun isAboveAvgBodySize(index: Int, windowForAvg: Int = 5): Boolean {

        return getCandleBodyMeasurement(index) > getAvgCandleBody(index, windowForAvg)
    }


    /**
     * Check if candle is above average high to low size
     */
    fun isAboveAvgHighToLowSize(index: Int, windowForAvg: Int = 5): Boolean {
        return getCandleHighToLowMeasurement(index) > getAvgCandleHighToLow(index, windowForAvg)
    }


    /**
     * Returns true if candle has is Green + no Tail + no Head + is Above Avg Body Size for span
     */
    fun isFullBody(index: Int): Boolean {
        return getHeadSizeAsPercentageOfTotalSize(index) < .05 &&
                getTailSizeAsPercentageOfTotalSize(index) < .05
    }


    /**
     * Returns true if candle is < 50% size of avg body, with both Head & Tail > 50% avg body
     */
    fun isPinBar(index: Int, windowForAvg: Int = 5): Boolean {
        val halfAvgBody = getAvgCandleBody(index, windowForAvg) * .5

        val head = getHeadSizeAsPercentageOfTotalSize(index)
        if (head < halfAvgBody) {
            return false
        }

        val tail = getTailSizeAsPercentageOfTotalSize(index)
        if (tail < halfAvgBody) {
            return false
        }

        if (getCandleBodyMeasurement(index) > halfAvgBody) {
            return false
        }

        return true
    }


    /**
     * Returns true if candle is Top Body Hammer. (Small Body + No Head + Big Tail)
     */
    fun isTopBodyHammer(index: Int): Boolean {


        // If big tail, return true
        if (getTailSizeAsPercentageOfTotalSize(index) > .5) {
            return true
        }

        return false
    }


    /**
     * Returns true if candle is Bottom Body Hammer. (Small Body + No Tail + Big Head)
     */
    fun isBottomBodyHammer(index: Int): Boolean {

        // If big head, return true
        if (getHeadSizeAsPercentageOfTotalSize(index) > .5) {
            return true
        }

        return false
    }


    companion object {

        // Creates Chart from Charles Schwab Api Historic Data
        fun fromCSHistoricData(csHistoricData: CharlesSchwabHistoricData): StockChartAdvanced {

            val convertCandles = {
                val loc = mutableListOf<Candle>()

                for (n in 0..csHistoricData.open.lastIndex){
                    loc.add(
                        Candle(
                            csHistoricData.datetime[n],
                            csHistoricData.timestamp[n],
                            csHistoricData.open[n],
                            csHistoricData.high[n],
                            csHistoricData.low[n],
                            csHistoricData.close[n],
                            csHistoricData.volume[n]
                        )
                    )
                }

                loc.toList()
            }

            return StockChartAdvanced(
                ticker = csHistoricData.ticker,
                candleSize = csHistoricData.candleSize,
                periodSize = csHistoricData.open.size,
                prepost = csHistoricData.prepost,
                datetime = csHistoricData.datetime,
                timestamp = csHistoricData.timestamp,
                open = csHistoricData.open,
                high = csHistoricData.high,
                low = csHistoricData.low,
                close = csHistoricData.close,
                volume = csHistoricData.volume,
                candles = convertCandles()
            )
        }
    }
}


fun main() {
    val c = Common.csApi.getHistoricData1day("SPY", 6, false)!!
    val sc = StockChartAdvanced.fromCSHistoricData(c)
    val cLast = c.open[c.open.lastIndex]
    val scLast = sc.getOpenAtIndex(-1)

    println(cLast)
    println(scLast)
}

