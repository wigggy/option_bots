package com.github.wigggy.examples

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class NavigationExample : Application() {
    override fun start(stage: Stage) {
        // Scene 1
        val button1 = Button("Go to Scene 2")
        val layout1 = StackPane()
        layout1.children.add(button1)
        val scene1 = Scene(layout1, 400.0, 300.0)

        // Scene 2
        val button2 = Button("Go to Scene 1")
        val layout2 = StackPane()
        layout2.children.add(button2)
        val scene2 = Scene(layout2, 400.0, 300.0)

        // Set up button actions to switch scenes
        button1.setOnAction {
            stage.scene = scene2
        }

        button2.setOnAction {
            stage.scene = scene1
        }

        // Initial scene setup
        stage.scene = scene1
        stage.title = "Scene Switch Example"
        stage.show()
    }
}

fun main() {
    Application.launch(NavigationExample::class.java)
}