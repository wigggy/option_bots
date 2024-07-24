package com.github.wigggy.examples


import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class MyAppViewModel {
    private val _state = MutableStateFlow("Initial State")
    val state: StateFlow<String> get() = _state

    fun updateState(newState: String) {
        _state.value = newState
    }
}



class MyApp : Application() {
    private val viewModel = MyAppViewModel()

    override fun start(primaryStage: Stage) {
        val label = Label()
        val button = Button("Update State")

        // Update viewmodel state on click
        button.setOnAction {
            viewModel.updateState("Updated State at ${System.currentTimeMillis()}")
        }

        val vbox = VBox(label, button)
        val scene = Scene(vbox, 300.0, 200.0)
        primaryStage.scene = scene
        primaryStage.show()

        // Observe the StateFlow and update the label text
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.state.collect { state ->
                label.text = state
            }
        }
    }
}

fun main() {
    Application.launch(MyApp::class.java)
}