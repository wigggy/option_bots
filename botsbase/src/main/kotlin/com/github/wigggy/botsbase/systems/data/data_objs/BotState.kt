package com.github.wigggy.botsbase.systems.data.data_objs

import java.util.*


/** Current state of Bot for the day, with some basic info on Bot. */
data class BotState(
    val botName: String = "",
    val botDesc: String = "DefaultBotDescription",
    val dateBotCreatedMMDDYYYY: String = "01/01/2022",
    val gainLossDollarDaysRealized: Double = 0.0,
    val gainLossPercentDaysRealized: Double = 0.0,        // Based off total cost of all closed pos vrs total gain of all pos
    val gainLossDollarDaysTotal: Double = 0.0,
    val gainLossDollarUnrealized: Double = 0.0,
    val gainLossDollarDaysRealizedAvg: Double = 0.0,
    val gainLossPercentDaysRealizedAvg: Double = 0.0,
    val nOpenPositions: Int = 0,
    val nClosedPositionsToday: Int = 0,
    val nTotalPositionsToday: Int = 0,
    val curCashPapertradeBalance: Double = 0.0,         // Changes with gains
    val originalPapertradeBalance: Double = 0.0, // Default starting balance
    val buyingPower: Double = 0.0,                // Default buying power
    val daysStartingBalance: Double = 0.0,
    val curOpenPositions: List<OptionPosition> = listOf(),
    val daysClosedPositions: List<OptionPosition> = listOf(),
    val tickerBlackList: List<String> = listOf(),
    val lastUpdateTimestampMs: Long = 0L,
    val lastUpdateDate: Date = Date(0L),
    val watchlist: List<String> = listOf(),
    val postCycleSleepTimeMs: Long = 5000L,
    val power: Boolean = false,
    val daysStartingBalanceLastUpdate: Long = 0L,
    val curTotalBalance: Double = 0.0,
    val tradePermCheck: TradePermissionCheckResults = TradePermissionCheckResults(),        // TODO Remove This

    val avgWinAmount: Double = 0.0,
    val avgLossAmount: Double = 0.0,
    val avgPercentGain: Double = 0.0,
    val avgPercentLoss: Double = 0.0,
    val daysWins: Int = 0,
    val daysLosses: Int = 0,
    val daysWinLossRatio: Double = 0.0,
    val daysWinPercentage: Double = 0.0,
    val daysBiggestGain: Double = 0.0,
    val daysBiggestLoss: Double = 0.0,
    val nPositionsOpenedToday: Int = 0,
    val daysTradedTicks: List<String> = listOf(),       // All Traded (open + closed)

    // Closed only
    val topGainingTicker: String = "None",
    val topGainingTickerGainDollar: Double = 0.0,
    val daysTickerGainMap: Map<String, Double> = mapOf(),
    val cycleCount: Int = 0,
)

// TODO -- Add win/loss stats
//  - win %, biggest win/loss, avg w/l, nWins/Losses

