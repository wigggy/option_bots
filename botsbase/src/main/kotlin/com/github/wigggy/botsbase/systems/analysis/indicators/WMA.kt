package com.github.wigggy.botsbase.systems.analysis.indicators

data class WMA(
    val input: List<Double>,
    val period: Int,
    val wmaValues: List<Double>
)
