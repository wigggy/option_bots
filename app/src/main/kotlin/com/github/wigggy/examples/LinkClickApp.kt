package com.github.wigggy.examples

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.awt.Desktop
import java.net.URI

class HyperlinkApp : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Hyperlink Example"

        val hyperlink = Hyperlink("Open Google")
        hyperlink.setOnAction {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI("https://www.google.com"))
            } else {
                println("Desktop is not supported. Cannot open the URL.")
            }
        }

        val vbox = VBox(hyperlink)
        val scene = Scene(vbox, 300.0, 200.0)

        primaryStage.scene = scene
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(HyperlinkApp::class.java, *args)
}