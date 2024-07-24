package com.github.wigggy.app.home

import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition


/* TODO
*   Consider using this
* */
data class HomeUIState(
    val botStates: Map<String, BotState> = mapOf(),
    val curBotnameOpenPosShowing: String = "",
    val curBotsOpenPosList: List<OptionPosition> = listOf()
)


