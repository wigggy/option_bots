package com.github.wigggy.botsbase.systems

import com.github.wigggy.botsbase.active_bots.BigMoveSniper
import com.github.wigggy.botsbase.active_bots.TestBot
import com.github.wigggy.botsbase.systems.bot_tools.BotToolsLogger
import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object BotManager {

    private val log = BotToolsLogger("BotManager")

    val coroutineScopeStateFlowCollection = CoroutineScope(SupervisorJob())

    private val _stateFlowMapOfBotState: MutableStateFlow<Map<String, BotState>> = MutableStateFlow(mapOf())
    val stateFlowMapOfBotState get() = _stateFlowMapOfBotState
    val botsList: List<BaseBot>


    init {
        // Add active bots to botsList
        botsList = buildListOfBots()

        // Observe each Bot's BotStates
        initObserveBotStates()
    }


    private fun buildListOfBots(): List<BaseBot> {
        return listOf(
            TestBot(),
            com.github.wigggy.botsbase.active_bots.BigMoveSniper()
        )
    }


    fun getCurMapOfBotStateValue(): Map<String, BotState> {
        return _stateFlowMapOfBotState.value
    }


    /** Launches a coroutine for each bot to observe their StateFlow<BotState>.
     * BotState's are added to _stateFlowMapOfBotState as Key/Value pairs of BotName/BotState */
    private fun initObserveBotStates() {

        // Loop through bots and launch a coroutine to observe each of their BotState Flows
        for (b in botsList) {
            coroutineScopeStateFlowCollection.launch {
                b.botState.collect { botState ->

                    // Update the BotManager._stateFlowMapOfBotState with new data
                    val oldState = _stateFlowMapOfBotState.value.toMutableMap()
                    oldState[botState.botName] = botState
                    val newState = oldState.toMap()
                    _stateFlowMapOfBotState.update {
                        newState
                    }
                }
            }
        }
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


//    fun turnAllBotsOff() {
//
//        log.w("turnAllBotsOff() Bot Threads Closing...Max 10s wait...")
//
//        // Power off and interrupt all bots
//        for (b in botsList) {
//            if (b.getPower()) {
//                b.powerOff()
//            }
//        }
//
//        // Wait for bots to shut down with a timeout of 10 seconds
//        for (n in 1..10) {
//
//            // Check if any thread is still running
//            val threadStillAlive = botsList.any { it.isAlive }
//
//            if (!threadStillAlive) {
//                log.w("\u001B[31m\u001B[1mAll Bot Threads Are Dead. Exiting Program...\u001B[0m")
//                break
//            }
//
//            print("\u001B[31m\u001B[1mWaiting for Bot Threads to Die... $n/10s\u001B[0m")
//            repeat(n) {
//                print("\u001B[31m\u001B[1m * \u001B[0m")
//            }
//            println()
//            Thread.sleep(1000)
//        }
//
//        // After the timeout, ensure all threads are properly joined
//        botsList.forEach { bot ->
//            try {
//                bot.join(5000) // Join with a timeout to avoid hanging indefinitely
//            } catch (e: InterruptedException) {
//                log.w("\u001B[31m\u001B[1mInterrupted while waiting for bot to finish.\u001B[0m")
//            }
//        }
//
//        log.w("\u001B[31m\u001B[1mBot Safe-Shutdown Complete.\u001B[0m")
//    }


//    fun turnAllBotsOff() {
//        for (b in botsList) {
//            if (b.isAlive){
//                b.powerOff()
//                b.shutDownThread()
//                b.interrupt()
//            }
//        }
//        log.i("turnAllBotsOff() Bot Threads Closing...10s wait...")
//
//        for (n in 1..10){
//
//            // Check if thread is still running
//            var threadStillAlive = false
//            for (b in botsList){
//                if (b.isAlive) {
//                    threadStillAlive = true
//                    break
//                }
//            }
//            if (!threadStillAlive) {
//                println("All Bot Threads Are Dead. Exiting Program...")
//                break
//            }
//
//            print("\u001B[31m\u001B[1mBot Safe-Shutdown Timer: $n/10\u001B[0m")
//            repeat(n){
//                print("\u001B[31m\u001B[1m...\u001B[0m")
//            }
//            println()
//            Thread.sleep(1000)
//        }
//        println("Bot Safe-Shutdown Complete.")
//    }


    fun startBots() {
        for (b in botsList) {
            // NOTE DONT USE DAEMON THREADS, the bot threads will be accessing the database
            b.start()
        }
    }


//    private fun addBotShutdownHook() {
//        Runtime.getRuntime().addShutdownHook(
//            Thread {
//                log.i("addBotShutdownHook() Shutdown Hook Triggered. Program Closing.")
////                turnAllBotsOff()
//            }
//        )
//    }

}















