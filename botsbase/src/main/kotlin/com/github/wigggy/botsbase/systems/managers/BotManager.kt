package com.github.wigggy.botsbase.systems.managers

import com.github.wigggy.botsbase.systems.BaseBot
import com.github.wigggy.botsbase.systems.bot_tools.ColorLogger
import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors

object BotManager {

    private val log = ColorLogger("BotManager")

    val nThreadsInBotStateCollectionDispatcher = 3
    val botStateCollectionDispatcher = createBotStateObservationDispatcher()
    val coroutineScopeStateFlowCollection = CoroutineScope(SupervisorJob() + botStateCollectionDispatcher)

    private val _stateFlowMapOfBotState: MutableStateFlow<Map<String, BotState>> = MutableStateFlow(mapOf())
    val stateFlowMapOfBotState get() = _stateFlowMapOfBotState
    private val botsList = mutableListOf<BaseBot>()
    private val botstateObserveJobs = mutableMapOf<String, Job>()


    init {
        coroutineShutdownHook()
    }


    private fun createBotStateObservationDispatcher(): CoroutineDispatcher {
        return Executors.newFixedThreadPool(nThreadsInBotStateCollectionDispatcher).asCoroutineDispatcher()
    }


    private fun coroutineShutdownHook(){
        Runtime.getRuntime().addShutdownHook(
            Thread{
                botStateCollectionDispatcher.cancel()
            }
        )
    }


    fun addBotToBotsList(bot: BaseBot) {
        if(botsList.contains(bot) == false){
            botsList.add(bot)
            startObservingBotState(bot)
        }
    }

    fun removeBotFromBotsList(bot: BaseBot) {
        botsList.remove(bot)

        // Cancel the BotState state flow collection
        val observeJob: Job? = botstateObserveJobs.get(bot.botName)
        observeJob?.cancel()
    }

    fun getCurMapOfBotStateValue(): Map<String, BotState> {
        return _stateFlowMapOfBotState.value
    }


    /** Launches a coroutine for each bot to observe their StateFlow<BotState>.
     * BotState's are added to _stateFlowMapOfBotState as Key/Value pairs of BotName/BotState */
    private fun startObservingBotState(bot: BaseBot) {

    // Loop through bots and launch a coroutine to observe each of their BotState Flows
        val j = coroutineScopeStateFlowCollection.launch {

            bot.botState.collectLatest { botState ->

                // Update the BotManager._stateFlowMapOfBotState with new data
                val oldState = _stateFlowMapOfBotState.value.toMutableMap()
                oldState[botState.botName] = botState
                _stateFlowMapOfBotState.update {
                    oldState.toMap()
                }
            }
        }

        // Add job to map so can be canceled if bot is removed
        botstateObserveJobs.put(bot.botName, j)
    }


    fun turnBotOn(botName: String) {
        for (b in botsList){
            if (b.botName == botName) {
                b.powerOn()
                return
            }
        }
    }


    fun turnBotOff(botName: String) {
        for (b in botsList){
            if (b.botName == botName) {
                b.powerOff()
                return
            }
        }
    }


    fun startBots() {
        for (b in botsList) {
            // NOTE DONT USE DAEMON THREADS, the bot threads will be accessing the database
            b.start()
        }
    }


}















