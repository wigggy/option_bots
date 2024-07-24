package com.github.wigggy.examples
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage

class FxmlApp : Application() {


    override fun start(stage: Stage) {
                                                                    // Make sure the '/' is before file name
                                                                    // Even if in root 'resources' folder
                                                                    // Exp: 'resources/my-view.fxml' is...
                                                                    // '/my-view.fxml'
        val fxmlLoader: VBox = FXMLLoader(FxmlApp::class.java.getResource("/exampleApp/hello-view.fxml")).load()
//        val fxmlLoader2 = FXMLLoader(javaClass.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader, 1024.0, 768.0)
        stage.title = "Hello!"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(FxmlApp::class.java)

}




