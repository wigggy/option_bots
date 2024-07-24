package com.github.wigggy.app.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger


fun doubleToTwoDecimalFormat(d: Double): String {
    val doubleTwoDecPlaces = String.format("%.2f", d)
    return doubleTwoDecPlaces
}


fun doubleToDollarFormat(d: Double): String {
    val doubleTwoDecPlaces = String.format("%.2f", d)
    return "$$doubleTwoDecPlaces"
}


fun doubleToPercentFormat(d: Double): String {
    val doubleTwoDecPlaces = String.format("%.2f", d)
    return "%$doubleTwoDecPlaces"
}


fun camelCaseToNormalCase(camelCaseString: String): String {
    // Use a regular expression to find places where a lowercase letter is followed by an uppercase letter
    val regex = "(?<=[a-z])(?=[A-Z])".toRegex()
    // Replace these positions with a space
    return camelCaseString.split(regex).joinToString(" ")
}


/** Collects from flow with try/catch block.
 *
 * @param owner The class calling function. Use 'this'. Used to report to Log on exception
 * @param scope CoroutineScope used for collection.
 * @param onFlowCollection Function block to be executed on collection*/
fun <T> Flow<T>.collectLatestSafe(owner: Any, scope: CoroutineScope, onFlowCollection: (T) -> Unit) {
    scope.launch {
        try {
            this@collectLatestSafe.collectLatest { value ->
                onFlowCollection(value)
            }
        } catch (e: Exception) {
            Logger.getGlobal().warning("tools.kt -> flowCollectLatestSafe()" +
                    " Owner: $owner Exception: ${e.message} ${e.stackTrace}")
        }
    }
}


fun timestampToHHMMAP_MMDDYYYY(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val formatter = DateTimeFormatter.ofPattern("hh:mma M/d/yyyy", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant).toLowerCase()
}


fun main() {



}