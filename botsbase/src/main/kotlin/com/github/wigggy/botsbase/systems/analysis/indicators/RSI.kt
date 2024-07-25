package com.github.wigggy.botsbase.systems.analysis.indicators

data class RSI(
    val input: List<Double>,
    val period: Int,
    val rsiValues: List<Double>
)
