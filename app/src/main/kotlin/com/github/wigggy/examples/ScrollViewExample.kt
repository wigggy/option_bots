package com.github.wigggy.examples

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.stage.Stage

class ScrollViewExample : Application() {

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
        val sp = ScrollPane()
        val rootVbox = VBox()

        for (i in 1..10) {
            val vbox = VBox().apply {
                prefHeight = 100.0
                prefWidth = 200.0
                children.add(
                    Label("Bot Scrollview Label $i")
                )
            }
            rootVbox.children.add(vbox)
        }
        sp.content = rootVbox
        return sp
    }

}

fun main() {
    Application.launch(ScrollViewExample::class.java)
}


