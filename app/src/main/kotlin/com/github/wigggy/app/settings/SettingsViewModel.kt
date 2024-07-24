package com.github.wigggy.app.settings

import com.github.wigggy.app.common.ViewModelDesktop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class SettingsViewModel: ViewModelDesktop {

    private val coroutineScope = CoroutineScope(Job())


    override fun destroy() {
        coroutineScope.cancel()
    }
}