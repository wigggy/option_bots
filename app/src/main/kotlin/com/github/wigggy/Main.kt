package com.github.wigggy

import com.github.wigggy.app.OptionBotsApp
import com.github.wigggy.bots.active_bots.TestBot
import com.github.wigggy.bots.active_bots.BigMoveSniper
import com.github.wigggy.botsbase.systems.managers.BotManager
import com.github.wigggy.botsbase.systems.managers.PosUpdateManager
import javafx.application.Application

fun main() {

    // Add Bots
    BotManager.addBotToBotsList(TestBot())
    BotManager.addBotToBotsList(BigMoveSniper())

    // Start Bots
    BotManager.startBots()

    // Start Open Pos background updates
    PosUpdateManager.startUpdaterThread()

    // Start App
    Application.launch(OptionBotsApp::class.java)
}