package com.github.wigggy.botsbase.systems.analysis.indicators

data class ParabolicSAR(
    val inputHigh: List<Double>,
    val inputLow: List<Double>,
    val accelerationStep: Double,
    val maximumStep: Double,
    val psarValues: List<Double>
)
