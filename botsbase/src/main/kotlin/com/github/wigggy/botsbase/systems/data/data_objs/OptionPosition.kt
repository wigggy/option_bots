package com.github.wigggy.botsbase.systems.data.data_objs

import java.util.*


data class OptionPosition(

    // Basics
    val id: UUID,
    val botName: String,
    val botDesc: String,
    val stockSymbol: String,
    val optionSymbol: String,
    val lastUpdatedTimestampMs: Long,
    val lastUpdatedDate: Date,
    val openTimestampMs: Long,
    val openDate: Date,
    val closeTimestampMs: Long,
    val closeDate: Date,
    val isPaperTrade: Boolean,
    val putCall: String,
    val strikePrice: Double,
    val lastTradingDay: Long,
    val expirationDate: String,
    val description: String,
    val dteAtPurchaseTime: Int,
    val quantity: Int,
    val fees: Double,       // .65cents per contract
    val pricePer: Double,
    val totalPrice: Double,

    // Stats
    val bid: Double,
    val ask: Double,
    val mark: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val openPrice: Double,
    val totalVolume: Int,
    val daysPercentChangeAtPurchaseTime: Double,
    val daysNetChangeAtPurchaseTime: Double,
    val volatility: Double,
    val delta: Double,
    val gamma: Double,
    val theta: Double,
    val vega: Double,
    val rho: Double,
    val openInterest: Int,
    val timeValue: Double,
    val theoreticalOptionValue: Double,
    val dte: Int,
    val intrinsicValue: Double,
    val high52Week: Double,
    val low52Week: Double,
    val inTheMoney: Boolean,
    val itmDistance: Double,


    // Gainloss
    val gainLossDollarTotal: Double,
    val gainLossDollarPer: Double,
    val gainLossPercent: Double,

    // TP / Stop
    val takeProfitDollarTarget: Double,
    val takeProfitPercentTarget: Double,
    val stopLossDollarTarget: Double,
    val stopLossPercentTarget: Double,
    val closeReason: String,

    val quoteAtOpenJson: String,
    val quoteAtCloseJson: String,
    val underlyingPriceCurrent: Double,
    val underlyingPriceAtPurchase: Double,
    val curValuePerContract: Double,
    val curValueOfPosition: Double,

    // New
    val highestGainDollarPerContract: Double,
    val highestGainDollarTotal: Double,
    val lowestGainDollarPerContract: Double,
    val lowestGainDollarPerTotal: Double,
    val highestGainPercent: Double,
    val lowestGainPercent: Double,

    val extraData: Map<String, String>

)

// Stats that change after purchase that should be updated
//val lastUpdatedTimestampMs: Long,
//val lastUpdatedDate: Date,
//val closeTimestampMs: Long,
//val closeDate: Date,
//val fees: Double,       // .65 for BUY, & .65 for SELL (1.30 total) (per contract)
//val bid: Double,
//val ask: Double,
//val mark: Double,
//val highPrice: Double,
//val lowPrice: Double,
//val totalVolume: Int,
//val volatility: Double,
//val delta: Double,
//val gamma: Double,
//val theta: Double,
//val vega: Double,
//val rho: Double,
//val openInterest: Int,
//val timeValue: Double,
//val theoreticalOptionValue: Double,
//val dte: Int,
//val intrinsicValue: Double,
//val high52Week: Double,
//val low52Week: Double,
//val inTheMoney: Boolean,
//val itmDistance: Double,
//val gainLossDollarTotal: Double,
//val gainLossDollarPer: Double,
//val gainLossPercent: Double,
//val quoteAtCloseJson: String,
//val underlyingPriceCurrent: Double,
//val curValuePerContract: Double,
//val curValueOfPosition: Double,
//val highestGainDollarPerContract: Double,
//val highestGainDollarTotal: Double,
//val lowestGainDollarPerContract: Double,
//val lowestGainDollarPerTotal: Double,
//val highestGainPercent: Double,
//val lowestGainPercent: Double,
