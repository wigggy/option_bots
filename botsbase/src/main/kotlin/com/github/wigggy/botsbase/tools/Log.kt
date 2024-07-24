package com.github.wigggy.botsbase.tools

import java.util.logging.Level
import java.util.logging.Logger

object Log {

    internal fun w(tag: String, msg: String? = null, stacktrace: String? = null) {
        Logger.getGlobal().log(Level.parse("WARNING"), formatLogMsg(tag, msg, stacktrace))
    }


    internal fun d(tag: String, msg: String?, stacktrace: String? = null) {
        Logger.getGlobal().log(Level.parse("DEBUG"), formatLogMsg(tag, msg, stacktrace))
    }


    internal fun i(tag: String, msg: String?, stacktrace: String? = null) {
        Logger.getGlobal().log(Level.parse("INFO"), formatLogMsg(tag, msg, stacktrace))
    }

    private fun formatLogMsg(tag: String, msg: String? = null, stacktrace: String? = null) : String {
        return "\n-- TAG: $tag --\n-- MESSAGE: $msg\n-- STACKTRACE: $stacktrace\n\n"
    }
}