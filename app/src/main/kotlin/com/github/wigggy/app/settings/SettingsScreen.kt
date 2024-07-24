package com.github.wigggy.app.settings

import com.github.wigggy.app.Navigator
import com.github.wigggy.app.common.ViewModelDesktop
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.text.Text

class SettingsScreen(viewModel: ViewModelDesktop, extraData: Map<String, String>? = null): VBox() {
    val testBtn = Button("Go HOME")

    init {

        testBtn.setOnAction {
            com.github.wigggy.app.Navigator.navigateToScreen("HOME")
        }


        this.children.add(Text("Settings"))
        this.children.add(testBtn)
    }

    companion object {
        const val SCREEN_NAME = "SETTINGS"
    }
}