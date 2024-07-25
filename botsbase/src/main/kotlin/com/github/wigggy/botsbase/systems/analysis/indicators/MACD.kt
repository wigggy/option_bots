package com.github.wigggy.botsbase.systems.analysis.indicators

data class MACD(
    val input: List<Double>,
    val fastPeriod: Int,
    val slowPeriod: Int,
    val signalPeriod: Int,
    val macdValues: List<Double>,
    val signalValues: List<Double>,
    val histogramValues: List<Double>
)
