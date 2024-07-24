package com.github.wigggy.app.login

data class LoginScreenState(
    val daysLeftTilLoginExpiry: Double = 0.0,
    val loginError: Boolean = false,
)
