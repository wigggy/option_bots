package com.github.wigggy.botsbase.systems.analysis.indicators

data class DirectionalMovmentIndex(
    val inputHigh: List<Double>,
    val inputLow: List<Double>,
    val inputClose: List<Double>,
    val period: Int,
    val plusDI: List<Double>,
    val minusDI: List<Double>,
    val adx: List<Double>
)
