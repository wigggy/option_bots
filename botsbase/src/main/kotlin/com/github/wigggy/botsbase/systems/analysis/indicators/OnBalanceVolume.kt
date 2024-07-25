package com.github.wigggy.botsbase.systems.analysis.indicators

data class OnBalanceVolume(
    val inputClose: List<Double>,
    val inputVolume: List<Int>,
    val obvValues: List<Double>
)
