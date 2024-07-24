package com.github.wigggy.examples

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.*

class ScrollPaneUpdateExample : Application() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var container: VBox
    private lateinit var scrollPane: ScrollPane

    override fun start(primaryStage: Stage) {
        container = VBox()
        scrollPane = ScrollPane(container)

        val root = VBox()
        val updateButton = Button("Update Items")
        updateButton.setOnAction {
            updateItems()
        }
        root.children.addAll(updateButton, scrollPane)

        val scene = Scene(root, 400.0, 600.0)
        primaryStage.scene = scene
        primaryStage.title = "ScrollPane Update Example"
        primaryStage.show()
    }

    private fun updateItems() {
        coroutineScope.launch {
            // Simulate some data fetching or processing
            delay(1000)
            val newItems = (1..20).map { Label("Updated Item $it") }

            // Save the current scroll position
            val hvalue = scrollPane.hvalue
            val vvalue = scrollPane.vvalue

            // Update the UI on the JavaFX Application Thread
            Platform.runLater {
                container.children.setAll(newItems)
                // Restore the scroll position
                scrollPane.hvalue = hvalue
                scrollPane.vvalue = vvalue
            }
        }
    }

    override fun stop() {
        coroutineScope.cancel() // Cancel the coroutine scope when the application stops
        super.stop()
    }
}

fun main() {
    Application.launch(ScrollPaneUpdateExample::class.java)
}