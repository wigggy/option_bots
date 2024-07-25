package com.github.wigggy.botsbase.systems.analysis.indicators

data class StandardDeviation(
    val input: List<Double>,
    val period: Int,
    val multiplier: Double,
    val stdValues: List<Double>
)
