package com.github.wigggy.botsbase.systems.analysis.indicators

import com.tictactec.ta.lib.MAType

data class BollingerBands(
    val input: List<Double>,
    val period: Int,
    val upperBand: List<Double>,
    val middleBand: List<Double>,
    val lowerBand: List<Double>
)
