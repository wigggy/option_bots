package com.github.wigggy.app.login

import com.github.wigggy.app.Navigator
import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.common.Screen
import com.github.wigggy.app.common.collectLatestSafe
import com.github.wigggy.app.home.HomeScreen
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import kotlinx.coroutines.*

class LoginScreen(vm: LoginViewModel, extraData: Map<String, String>? = null): Screen, VBox() {

    // Set up like this so viewmodel reference can be cleared in onDestroy()
    private var _viewModel: LoginViewModel? = vm
    private val viewModel get() = _viewModel!!

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    private val header = buildHeader()
    private val daysLeftText = buildLoginDaysLeft()
    private val buttonGoToCsLogin = buildGoToCsLoginButton()
    private val textField = buildTextField()
    private val buttonLoginWithCode = buildLoginWithCodeButton()
    private val loginErrorLabel = buildLoginErrorLabel()

    init {
        this.prefHeight = 1024.0
        this.prefWidth = 768.0
        this.alignment = Pos.CENTER
        this.children.add(header)
        this.children.add(daysLeftText)
        this.children.add(buttonGoToCsLogin)
        this.children.add(textField)
        this.children.add(buttonLoginWithCode)
        this.children.add(loginErrorLabel)
        this.children.add(buildGoToHomeButton())
        setBackgroundColor()
        initScreenStateFlowObserver()

    }


    private fun setBackgroundColor(){
        this.background = Background(BackgroundFill(javafx.scene.paint.Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))

    }


    private fun initScreenStateFlowObserver() {

        viewModel.loginScreenState.collectLatestSafe(this, coroutineScope){ it ->

            daysLeftText.text = it.daysLeftTilLoginExpiry.toString() + " Days Remaining Before Login Invalid."
            if(it.loginError){
                loginErrorLabel.text = "Error Logging In. Please Try Again."
            }
        }
    }

    private fun buildHeader(): Label {
        val t = Label("Login")
        t.font = Font.font(72.0)
        t.padding = Insets(60.0, 0.0, 60.0, 0.0)
        return t
    }

    private fun buildLoginDaysLeft(): Label {
        val l = Label()
        l.font = Font.font(28.0)
        l.padding = Insets(28.0)
        return l
    }


    private fun buildGoToCsLoginButton(): Pane {
        val root = VBox()
        root.padding = Insets(20.0)
        root.alignment = Pos.CENTER

        val btn = Button("Go To CS Login").apply {
            setOnAction {
                viewModel.openLoginPageInBrowser()
            }
        }

        root.children.add(btn)
        return root
    }

    private fun buildLoginWithCodeButton(): Pane {
        val root = VBox()
        root.padding = Insets(20.0)
        root.alignment = Pos.CENTER

        val btn = Button("Login With Url Code").apply {
            setOnAction {
                viewModel.loginWithCode(textField.text) {
                    Platform.runLater{
                        com.github.wigggy.app.Navigator.navigateToScreen(HomeScreen.SCREEN_NAME, )
                    }
                }
            }
        }
        root.children.add(btn)
        return root
    }

    private fun buildGoToHomeButton(): Pane {
        val root = VBox()
        root.padding = Insets(20.0)
        root.alignment = Pos.CENTER
        val btn = Button("Go Home").apply {
            setOnAction {
                viewModel.goHome()
            }
        }
        root.children.add(btn)
        return root
    }

    private fun buildTextField(): TextField {
        val t = TextField()
        val w = AppValues.APP_WIDTH - (AppValues.APP_WIDTH * .30)
        t.maxWidth = w
        return t
    }

    private fun buildLoginErrorLabel(): Label {
        val l = Label("Please Login")
        l.font = Font.font("System", FontWeight.BOLD, 20.0)

        l.padding = Insets(28.0)
        return l
    }


    override fun destroy() {
        coroutineScope.cancel()
        _viewModel?.destroy()
        _viewModel = null
    }

    companion object {
        const val SCREEN_NAME = "LOGIN_SCREEN"
    }
}