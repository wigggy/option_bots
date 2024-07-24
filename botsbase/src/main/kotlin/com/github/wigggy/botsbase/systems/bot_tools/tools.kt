package com.github.wigggy.botsbase.systems.bot_tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.logging.Logger


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