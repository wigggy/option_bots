package com.github.wigggy.botsbase.systems.data.data_objs

data class TradePermissionCheckResults(
    val isOkToOpenPositions: Boolean = false,
    val isOkToClosePositions: Boolean = true,
    val shouldPerformEndOfDayCloseout: Boolean = false,

)
