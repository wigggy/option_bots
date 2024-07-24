package com.github.wigggy.examples
import javafx.fxml.FXML
import javafx.scene.control.Label

class HelloController {


    @FXML
    private lateinit var welcomeText: Label

    @FXML
    private fun onHelloButtonClick() {
        welcomeText.text = "Yooo"
    }
}