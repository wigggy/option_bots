package com.github.wigggy.botsbase.systems.analysis.indicators

data class SMA(
    val input: List<Double>,
    val period: Int,
    val smaValues: List<Double>
)
