package com.github.wigggy.app.login

import com.github.wigggy.app.Navigator
import com.github.wigggy.app.common.ViewModelDesktop
import com.github.wigggy.app.home.HomeScreen
import com.github.wigggy.botsbase.systems.bot_tools.Common
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.awt.Desktop
import java.net.URI
import java.util.logging.Logger

class LoginViewModel: ViewModelDesktop {

    private val coroutineScope = CoroutineScope(Job())
    private val _loginState = MutableStateFlow(LoginScreenState())
    val loginScreenState get() = _loginState


    init {
        updateRefreshTokenTimeInDays()
    }


    fun openLoginPageInBrowser() {
        Desktop.getDesktop().browse(URI(Common.csApi.loginUrl()))
    }


    private fun updateRefreshTokenTimeInDays(){
        val rtokenExpiry = Common.csApi.refreshTokenExpiry()
        val timeLeft =  rtokenExpiry.toDouble() - System.currentTimeMillis()
        val days = timeLeft.toDouble() / 86_400_000.toDouble()
        val daysTwoDecPoints = String.format("%.2f", days).toDouble()
        _loginState.update {s ->
            s.copy(daysLeftTilLoginExpiry = daysTwoDecPoints)
        }
    }

    fun loginWithCode(code: String, onSuccess: () -> Unit) {
        try {
            coroutineScope.launch {
                val success = Common.csApi.loginWithCode(code)

                if (success) {
                    onSuccess()
                }else {
                    _loginState.update {
                        it.copy(loginError = true)
                    }
                }
            }
        } catch (e: Exception){
            Logger.getGlobal().warning("'LoginScreenViewModel.loginWithCode' " +
                    "Exception. ${e.message} ${e.stackTrace}")
        }

    }

    fun goHome(){
        com.github.wigggy.app.Navigator.navigateToScreen(HomeScreen.SCREEN_NAME)
    }

    override fun destroy() {

        // Cancel coroutinescope
        coroutineScope.cancel()
    }
}