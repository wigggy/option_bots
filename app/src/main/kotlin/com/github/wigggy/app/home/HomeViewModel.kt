package com.github.wigggy.app.home

import com.github.wigggy.app.common.ViewModelDesktop
import com.github.wigggy.app.common.collectLatestSafe
import com.github.wigggy.app.settings.SettingsScreen
import com.github.wigggy.botsbase.systems.managers.BotManager
import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class HomeViewModel: ViewModelDesktop {



    private val crJob = Job() + Dispatchers.Unconfined
    private val coroutineScope = CoroutineScope(crJob)

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState get() = _uiState.asStateFlow()

    private val _botStateMapStateFlow = MutableStateFlow<Map<String, BotState>>(mapOf())

    init {
        initUiState()
        observeBotStateStateFlow()
    }


    /** Sets up the uiState incase the BotManager StateFlow doesn't 'emit' anything for a long period of time */
    private fun initUiState() {

        // Get latest value of BotStateMap
        val bs = BotManager.getCurMapOfBotStateValue()
        _botStateMapStateFlow.value = bs

        // Update _uiState if no Open Pos list is showing
        if (_uiState.value.curBotnameOpenPosShowing == ""){
            _uiState.update {
                it.copy(
                    curBotnameOpenPosShowing = bs.keys.first(),
                    curBotsOpenPosList = bs[bs.keys.first()]!!.curOpenPositions,
                    botStates = bs
                )
            }
        }

        // Update current _uiState's visible open pos list
        else {

            val curBotOpenShowing = _uiState.value.curBotnameOpenPosShowing
            _uiState.update {
                it.copy(
                    curBotsOpenPosList = bs[curBotOpenShowing]!!.curOpenPositions,
                    botStates = bs
                )
            }
        }
    }


    private fun observeBotStateStateFlow() {

        BotManager.stateFlowMapOfBotState.collectLatestSafe(this, coroutineScope) { bs ->
            // Update _uiState if no Open Pos list is showing
            if (_uiState.value.curBotnameOpenPosShowing == ""){
                _uiState.update {
                    it.copy(
                        curBotnameOpenPosShowing = bs.keys.first(),
                        curBotsOpenPosList = bs[bs.keys.first()]!!.curOpenPositions,
                        botStates = bs
                    )
                }
            }

            // Update current _uiState's visible open pos list
            else {

                val curBotOpenShowing = _uiState.value.curBotnameOpenPosShowing
                _uiState.update {
                    it.copy(
                        curBotsOpenPosList = bs[curBotOpenShowing]!!.curOpenPositions,
                        botStates = bs
                    )
                }

            }
        }

    }

    fun changeOpenPosList(botname: String){
        val listOfPos = _botStateMapStateFlow.value.get(botname)?.curOpenPositions ?: return
        _uiState.update { it ->
            it.copy(
                curBotnameOpenPosShowing = botname,
                curBotsOpenPosList = listOfPos
            )
        }
    }


    fun navigateToSettings(botname: String){
        val extraData = mapOf("botName" to botname)
        com.github.wigggy.app.Navigator.navigateToScreen(SettingsScreen.SCREEN_NAME, extraData)
    }

    override fun destroy() {

        // Cancel coroutinescope
        coroutineScope.cancel()
    }

}