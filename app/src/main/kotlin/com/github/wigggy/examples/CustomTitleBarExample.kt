package com.github.wigggy.examples

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

class CustomTitleBarExample : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.initStyle(StageStyle.UNDECORATED)

        // Create custom title bar
        val titleBar = HBox().apply {
            style = "-fx-background-color: #FF6347;" // Tomato color
            prefHeight = 30.0
            children.add(Button("Close").apply {
                setOnAction {
                    primaryStage.close()
                }
            })
        }

        // Add dragging functionality
        val root = StackPane().apply {
            children.add(titleBar)
            setOnMousePressed { event ->
                xOffset = event.sceneX
                yOffset = event.sceneY
            }
            setOnMouseDragged { event ->
                primaryStage.x = event.screenX - xOffset
                primaryStage.y = event.screenY - yOffset
            }
        }

        val scene = Scene(root, 400.0, 300.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    private var xOffset = 0.0
    private var yOffset = 0.0
}

fun main() {
    Application.launch(CustomTitleBarExample::class.java)
}