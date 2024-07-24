package com.github.wigggy.botsbase.tools

fun calculatePercentageGain(initialValue: Double, finalValue: Double): Double {
    if (initialValue == 0.0) {
        return 0.0
    }
    return ((finalValue - initialValue) / initialValue) * 100
}


fun doubleToTwoDecimalFormat(d: Double): String {
    val doubleTwoDecPlaces = String.format("%.2f", d)
    return doubleTwoDecPlaces
}


fun List<Double>.safeAverage(): Double{
    if (this.isEmpty()){
        return 0.0
    } else {
        return this.average()
    }
}