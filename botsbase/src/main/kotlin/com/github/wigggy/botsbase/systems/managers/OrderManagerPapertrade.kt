package com.github.wigggy.botsbase.systems.managers

import com.github.wigggy.botsbase.systems.data.OptionPositionDb
import com.github.wigggy.botsbase.systems.bot_tools.BotToolsLogger
import com.github.wigggy.botsbase.systems.bot_tools.Common
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import com.github.wigggy.botsbase.systems.interfaces.OrderManager
import com.github.wigggy.charles_schwab_api.data_objs.OptionQuote
import java.util.*
import kotlin.math.round
import kotlin.random.Random

class OrderManagerPapertrade(private val db: OptionPositionDb): OrderManager {

    private val log = BotToolsLogger(OrderManagerPapertrade::class.java.simpleName)

    private val limitOrderSimRandomAmountExtraMin = 0.0
    private val limitOrderSimRandomAmountExtraMax = 0.5


    /** Used to randomize how much above ask/ below bid to +/- on orders...
     *
     * Note: The Reason for this is: When i submit a real-trade Limit Order
     * i always add extra or - extra to make sure order goes through. This helps simulate that*/
    private fun limitOrderExtraRandomizer(): Double {
        val randomValue = Random.nextDouble(limitOrderSimRandomAmountExtraMin, limitOrderSimRandomAmountExtraMax)
        return round(randomValue * 100) / 100
    }


    override fun buyOrder(
        botName: String,
        bot_desc: String,
        option_symbol: String,
        quantity: Int,
        tp_dollar: Double,
        tp_pct: Double,
        stop_dollar: Double,
        stop_pct: Double,
        extra_data: Map<String, String>
    ): OptionPosition? {

        val getExpDate = { quote: OptionQuote ->

            val nums = quote.symbol.split(" ").last()
            val d = nums.substring(0, 6)
            val y = d.substring(0, 2)
            val m = d.substring(2, 4)
            val da = d.substring(4, 6)
            val date = "$m-$da-$y"
            date
        }


        val quote = Common.csApi.getOptionQuote(option_symbol)
        if (quote == null){
            log.w("buyOrder() Failed to open position. Null return on quote for $option_symbol. ")
            return null
        }

        // Simulate a limit order where you offer more than ask
        val costPer = quote.askPrice + limitOrderExtraRandomizer()

        val putOrCall = isPutOrCall(quote.symbol)
        val curTime = System.currentTimeMillis()
        val newPos = OptionPosition(
            id = UUID.randomUUID(),
            botName = botName,
            botDesc = bot_desc,
            stockSymbol = quote.symbol.split(" ").first(),
            optionSymbol = quote.symbol,
            lastUpdatedTimestampMs = curTime,
            lastUpdatedDate = java.util.Date(curTime),
            openTimestampMs = curTime,
            openDate = java.util.Date(curTime),
            closeTimestampMs = 0,
            closeDate = java.util.Date(0),
            isPaperTrade = true,
            putCall = putOrCall,
            strikePrice = quote.strikePrice,
            lastTradingDay = quote.lastTradingDay,
            expirationDate = getExpDate(quote),
            description = quote.description,
            dteAtPurchaseTime = quote.daysToExpiration,
            quantity = quantity,
            fees = quantity.toDouble() * 1.3,
            pricePer = costPer + 1.3,
            totalPrice = (costPer * quantity.toDouble()) + (quantity.toDouble() * 1.3),
            bid = quote.bidPrice,
            ask = quote.askPrice,
            mark = quote.mark,
            highPrice = quote.highPrice,
            lowPrice = quote.lowPrice,
            openPrice = quote.openPrice,
            totalVolume = quote.totalVolume,
            daysPercentChangeAtPurchaseTime = quote.markPercentChange,
            daysNetChangeAtPurchaseTime = quote.netPercentChange,
            volatility = quote.volatility,
            delta = quote.delta,
            gamma = quote.gamma,
            theta = quote.theta,
            vega = quote.vega,
            rho = quote.rho,
            openInterest = quote.openInterest,
            timeValue = quote.timeValue,
            theoreticalOptionValue = quote.theoreticalOptionValue,
            dte = quote.daysToExpiration,
            intrinsicValue = quote.moneyIntrinsicValue,
            high52Week = quote.weekHigh52,
            low52Week = quote.weekLow52,
            inTheMoney = itmCheck(putOrCall, quote.strikePrice, quote.underlyingPrice),
            itmDistance = itmDistanceCheck(putOrCall, quote.strikePrice, quote.underlyingPrice),
            gainLossDollarTotal = 0.0,
            gainLossDollarPer = 0.0,
            gainLossPercent = 0.0,
            takeProfitDollarTarget = tp_dollar,
            takeProfitPercentTarget = tp_pct,
            stopLossDollarTarget = stop_dollar,
            stopLossPercentTarget = stop_pct,
            closeReason = "POSITION IS OPEN",
            quoteAtOpenJson = Common.gson.toJson(quote),
            quoteAtCloseJson = "",
            underlyingPriceCurrent = quote.underlyingPrice,
            underlyingPriceAtPurchase = quote.underlyingPrice,
            curValuePerContract = quote.bidPrice - .65,
            curValueOfPosition = (quote.bidPrice * quantity.toDouble()) + (.65 * quantity.toDouble()),
            highestGainDollarPerContract = 0.0,
            highestGainDollarTotal = 0.0,
            lowestGainDollarPerContract = 0.0,
            lowestGainDollarPerTotal = 0.0,
            highestGainPercent = 0.0,
            lowestGainPercent = 0.0,
            extraData = extra_data
        )
        PosUpdateManager.addOptionSymbolToUpdateRequests(option_symbol)
        val s = db.insertOptionPosition(newPos)
        if (!s){
            log.w("buyOrder() Failed to open position. Database Insert Error.")
            return null
        }
        return newPos
    }


    override fun sellOrder(
        optionPos: OptionPosition,
        closeReason: String,
        extraClosingData: String
    ): OptionPosition? {

        // NOTE
        //      ALL FEES ARE ADDED IN BUY
        //      BOTH BUY AND SELL FEES ARE ADDED IN THE BUY FUNCTION

        // get quote
        val q = Common.csApi.getOptionQuote(optionPos.optionSymbol)
        if (q == null){
            log.w("sellOrder() Failed to close position. Null return on quote for ${optionPos.optionSymbol}. ")
            return null
        }

        // Add extra data if any
        val extraData = optionPos.extraData.toMutableMap()
        extraData["extraClosingData"] = extraClosingData

        val sellPrice = q.bidPrice - limitOrderExtraRandomizer()

        // Update pos dollar/pct values
        val totalCost = optionPos.totalPrice
        val quan = optionPos.quantity
        val gainDollarTotal = (sellPrice * quan.toDouble()) - totalCost
        val gainDollarPer = sellPrice - optionPos.pricePer
        val gainPercent = (((sellPrice) - optionPos.pricePer) / optionPos.pricePer) * 100
        val closeTs = System.currentTimeMillis()
        val closeD = java.util.Date(closeTs)

        val valOfPos = (sellPrice * quan.toDouble()) - optionPos.fees
        val valPer = valOfPos / quan.toDouble()

        val highGdPerCon =
            if (gainDollarPer > optionPos.highestGainDollarPerContract) gainDollarPer
            else optionPos.highestGainDollarPerContract
        val highGdTotal =
            if (gainDollarTotal > optionPos.highestGainDollarTotal) gainDollarTotal
            else optionPos.highestGainDollarTotal

        val lowestGdPerCon =
            if (gainDollarPer < optionPos.lowestGainDollarPerContract) gainDollarPer
            else optionPos.lowestGainDollarPerContract
        val lowestGdTotal =
            if (gainDollarTotal < optionPos.lowestGainDollarPerTotal) gainDollarTotal
            else optionPos.lowestGainDollarPerTotal

        val highestGPct =
            if (gainPercent > optionPos.highestGainPercent) gainPercent
            else optionPos.highestGainPercent
        val lowGPct =
            if (gainPercent < optionPos.lowestGainPercent) gainPercent
            else optionPos.lowestGainPercent

        val closedPos = optionPos.copy(
            lastUpdatedTimestampMs = closeTs,
            lastUpdatedDate = Date(closeTs),
            gainLossDollarTotal = gainDollarTotal,
            gainLossDollarPer = gainDollarPer,
            gainLossPercent = gainPercent,
            closeTimestampMs = closeTs,
            closeDate = closeD,
            closeReason = closeReason,
            quoteAtCloseJson = Common.gson.toJson(q),
            underlyingPriceCurrent = q.underlyingPrice,
            curValuePerContract = valPer,
            curValueOfPosition = valOfPos,
            bid = q.bidPrice,
            ask = q.askPrice,
            mark = q.mark,
            highPrice = q.highPrice,
            lowPrice = q.lowPrice,
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
            inTheMoney = itmCheck(
                isPutOrCall(q.symbol),
                q.strikePrice,
                q.underlyingPrice
            ),
            itmDistance = itmDistanceCheck(
                isPutOrCall(q.symbol),
                q.strikePrice,
                q.underlyingPrice
            ),
            highestGainDollarPerContract = highGdPerCon,
            highestGainDollarTotal = highGdTotal,
            highestGainPercent = highestGPct,
            lowestGainDollarPerContract = lowestGdPerCon,
            lowestGainDollarPerTotal = lowestGdTotal,
            lowestGainPercent = lowGPct
        )

        // Update pos in database and update manager
        PosUpdateManager.removeOptionSymbolFromUpdateRequests(closedPos.optionSymbol)
        val s = db.updateOptionPosition(closedPos)
        if (!s){
            log.w("sellOrder() Failed to close position. Database Update Error.")
        }
        return closedPos

    }


    private fun itmCheck(putCall: String, strike: Double, curStockValue: Double): Boolean {
        if (putCall == "CALL") {
            return if (strike > curStockValue) false else true
        } else {
            return if (strike > curStockValue) true else false
        }
    }


    private fun isPutOrCall(optionSymbol: String): String {
        val putCall = optionSymbol[12]
        return if (putCall == 'C') "CALL" else "PUT"
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