package com.github.wigggy.botsbase.systems.analysis.indicators

data class EMA(
    val input: List<Double>,
    val period: Int,
    val emaValues: List<Double>
)
