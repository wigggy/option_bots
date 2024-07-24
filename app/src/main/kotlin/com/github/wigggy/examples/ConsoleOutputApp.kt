package com.github.wigggy.examples

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.OutputStream
import java.io.PrintStream


/** Basic app showing how to show StdOut in an application */
class ConsoleOutput(private val textArea: TextArea) : OutputStream() {
    override fun write(b: Int) {
        Platform.runLater {
            textArea.appendText(b.toChar().toString())
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val text = String(b, off, len)
        Platform.runLater {
            textArea.appendText(text)
        }
    }
}

class ConsoleOutputApp : Application() {

    override fun start(primaryStage: Stage) {
        val textArea = TextArea()
        textArea.isEditable = false

        // Redirect stdout to the TextArea
        val ps = PrintStream(ConsoleOutput(textArea))
        System.setOut(ps)

        val root = VBox(textArea)
        val scene = Scene(root, 600.0, 400.0)

        primaryStage.title = "JavaFX Console Output"
        primaryStage.scene = scene
        primaryStage.show()

        // Example of writing to stdout
        println("Hello, World!")
        println("This will appear in the TextArea!")
    }
}

fun main() {
    Application.launch(ConsoleOutputApp::class.java)
    println("YOOO")
}
