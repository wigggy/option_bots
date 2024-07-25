package com.github.wigggy.botsbase.systems.analysis.stock_chart_analysis

import java.util.*

data class Candle(
    val datetime: Date,
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Int
)