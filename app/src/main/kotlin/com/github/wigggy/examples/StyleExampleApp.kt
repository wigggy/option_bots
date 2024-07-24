package com.github.wigggy.examples

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.random.Random

class StyleExampleApp : Application() {

    private val rootLayoutBorderPane = BorderPane()


    override fun start(stage: Stage) {
        val scene = Scene(rootLayoutBorderPane, 1024.0, 768.0)
        stage.title = "Option Bot"
        stage.scene = scene

        rootLayoutBorderPane.top = titleBar()
        rootLayoutBorderPane.left = botScrollView()
        stage.show()
    }

    private fun titleBar(): HBox {
        val t = Label("Title Bar Here")
        t.style = """
            -fx-text-fill: #FFFFFF;
            -fx-font-size: 24px;
        """.trimIndent()
        return HBox().apply {
            children.add(t)
            style = "-fx-background-color: #000FFF;"
        }
    }

    private fun botScrollView(): ScrollPane {
        // In order for scrollpane to scroll, it's ONE child must be Taller than the scrollpane
        val sp = ScrollPane()
        val rootVbox = VBox()

        for (i in 1..10) {

            // Generate Random Color
            val random = Random.Default
            val red = random.nextInt(256)
            val green = random.nextInt(256)
            val blue = random.nextInt(256)
            val randomColor = String.format("#%02X%02X%02X", red, green, blue)

            val vbox = VBox().apply {
                prefHeight = 100.0
                prefWidth = 200.0
                children.add(
                    Label("Bot Scrollview Label $i")
                )
                style = """
                    -fx-background-color: $randomColor;
                """.trimIndent()
            }
            rootVbox.children.add(vbox)
        }
        sp.content = rootVbox
        return sp
    }

}

fun main() {
    Application.launch(StyleExampleApp::class.java)
}