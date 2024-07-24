package com.github.wigggy.app

import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.login.LoginScreen
import com.github.wigggy.botsbase.systems.BotManager
import com.github.wigggy.botsbase.systems.PosUpdateManager
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kotlin.system.exitProcess


class OptionBotsApp: Application() {

    // Root node aka layout
    val rootStackPane = StackPane()


    override fun start(stage: Stage) {

        // Setup Nav
        initNavigation()

        // Show Scene
        val scene = Scene(rootStackPane, AppValues.APP_HEIGHT, AppValues.APP_WIDTH)
        stage.title = "Option Bot"
        stage.scene = scene
        stage.show()

    }


    fun initNavigation() {

        Navigator.initNavigator(rootStackPane)

        // Set home to show
        Navigator.navigateToScreen(LoginScreen.SCREEN_NAME)
    }


    override fun stop() {
        super.stop()
        exitProcess(0)      // Needs to be here or BotManager shutdownHook won't trigger
    }
}

fun main() {
    BotManager.startBots()
    PosUpdateManager.startUpdaterThread()
    Application.launch(OptionBotsApp::class.java)
}