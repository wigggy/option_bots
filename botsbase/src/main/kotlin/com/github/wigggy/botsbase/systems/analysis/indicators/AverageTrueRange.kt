package com.github.wigggy.botsbase.systems.analysis.indicators

data class AverageTrueRange(
    val inputHigh: List<Double>,
    val inputLow: List<Double>,
    val inputClose: List<Double>,
    val period: Int,
    val atrValues: List<Double>
)
