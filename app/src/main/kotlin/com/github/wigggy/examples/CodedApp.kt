package com.github.wigggy.examples

import com.github.wigggy.botsbase.systems.bot_tools.Common
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage

class CodedApp : Application() {
    val btn = Button()

    var tick = 0
    val t = Text(tick.toString())

    override fun start(stage: Stage) {
        val vbox = VBox()
        vbox.alignment = Pos.CENTER
        vbox.children.add(t)
        btn.text = "Click Me"
        btn.setOnAction {
            mThreadupdateExample()
//            btn.text = "yoo"
        }
        test()
        vbox.children.add(btn)
        val scene = Scene(vbox, 320.0, 240.0)
        stage.title = "Hello!"
        stage.scene = scene
        stage.show()
    }

    fun test() {
        val t = Thread {
            while (tick < 20){
                tick ++
                Thread.sleep(100)
            }
        }
        t.isDaemon = true       // Important -- This will cause the thread to close when JVM stops
        t.start()
    }

    fun mThreadupdateExample() {
        val t = Thread {
            // Use this to update from another thread
            val x = Common.csApi.getStockQuote("SPY")
            Platform.runLater {
//                t.text = tick.toString()
                t.text = x?.symbol
            }
        }
        t.start()
    }
}

fun main() {
    Application.launch(CodedApp::class.java)

}