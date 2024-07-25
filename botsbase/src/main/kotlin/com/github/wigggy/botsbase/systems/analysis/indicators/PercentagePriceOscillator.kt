package com.github.wigggy.botsbase.systems.analysis.indicators

data class PercentagePriceOscillator(
    val input: List<Double>,
    val fastPeriod: Int,
    val slowPeriod: Int,
    val ppoValues: List<Double>
)
