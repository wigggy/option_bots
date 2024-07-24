package com.github.wigggy.app

import com.github.wigggy.app.common.Screen
import com.github.wigggy.app.common.ViewModelDesktop
import com.github.wigggy.app.home.HomeScreen
import com.github.wigggy.app.home.HomeViewModel
import com.github.wigggy.app.login.LoginScreen
import com.github.wigggy.app.login.LoginViewModel
import javafx.scene.layout.StackPane

object Navigator {


    private lateinit var rootStackpaner: StackPane

    // When navigating to new screen, the curViewModel.onDestroy() fun will be called.
    //      then, curViewModel is replaced with new viewmodel (navigateToScreen())
    private var curScreen: Screen? = null

    fun initNavigator(stackPane: StackPane) {
        com.github.wigggy.app.Navigator.rootStackpaner = stackPane
    }


    fun navigateToScreen(name: String, extraData: Map<String, String>? = null) {

        // Clear old screen from app's Root Node
        com.github.wigggy.app.Navigator.rootStackpaner.children.clear()

        // Call onDestroy() for cur screen so cleanup can occur, Screen's destroy their own viewmodels
        com.github.wigggy.app.Navigator.curScreen?.destroy()
        System.gc()
        when (name) {
            LoginScreen.SCREEN_NAME -> {

                // Set curVm and curScreen, and pass on 'extraData'
                com.github.wigggy.app.Navigator.curScreen = LoginScreen(LoginViewModel(), extraData)

                // Add curScreen to root
                com.github.wigggy.app.Navigator.rootStackpaner.children.add(com.github.wigggy.app.Navigator.curScreen as LoginScreen)
            }
            HomeScreen.SCREEN_NAME -> {

                // Set curVm and curScreen, and pass on 'extraData'
                com.github.wigggy.app.Navigator.curScreen = HomeScreen(HomeViewModel(), extraData)

                // Add curScreen to root
                com.github.wigggy.app.Navigator.rootStackpaner.children.add(com.github.wigggy.app.Navigator.curScreen as HomeScreen)
            }
        }
    }

}

