package com.github.wigggy.botsbase.systems.data.data_objs

data class AnalysisResult (
    val ticker: String,
    val triggerValue: Int,      // -1, 0, 1    -- Short, NoBuy, Long --
    val suggestedStopDollar: Double,
    val suggestedStopPct: Double,
    val suggestedTakeProfitDollar: Double,
    val suggestedTakeProfitPct: Double,

)