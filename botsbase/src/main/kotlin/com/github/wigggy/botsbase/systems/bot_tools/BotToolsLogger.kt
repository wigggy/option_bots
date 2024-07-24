package com.github.wigggy.botsbase.systems.bot_tools

import java.io.BufferedWriter
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BotToolsLogger(private val ownerName: String) {

    private val red = "\u001B[31m"
    private val blue = "\u001B[34m"
    private val cyan = "\u001B[36m"
    private val reset = "\u001B[0m"
    private val bold = "\u001B[1m"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val writer: BufferedWriter = initBufferedWriter()
    private val lockWriter = Any()


    private fun initBufferedWriter(): BufferedWriter {
        val f =
            if (ownerName.contains(".") == false) {
                ownerName + ".txt"
            }
            else {
                ownerName
            }

        val p = "_bot_logs\\$f"

        return BufferedWriter(FileWriter(p, true))
    }


    /** Performs thread-safe write to log file and output to console */
    private fun writeToFile(txt: String) {
        Thread {
            synchronized(lockWriter) {
                println(txt)
                writer.write(txt)
                writer.newLine()
                writer.flush()
            }
        }.start()
    }

    fun i(msg: String) {
        val dts = LocalDateTime.now().format(dateTimeFormatter)
        val s = "$bold$blue[$dts] [INFO] [$ownerName] $msg $reset"
        writeToFile(s)
    }

    fun w(msg: String) {
        val dts = LocalDateTime.now().format(dateTimeFormatter)
        val s = "$bold$red[$dts] [WARN] [$ownerName] $msg $reset"
        writeToFile(s)
    }

    fun d(msg: String) {
        val dts = LocalDateTime.now().format(dateTimeFormatter)
        val s = "$bold$cyan[$dts] [DBUG] [$ownerName] $msg $reset"
        writeToFile(s)
    }

}