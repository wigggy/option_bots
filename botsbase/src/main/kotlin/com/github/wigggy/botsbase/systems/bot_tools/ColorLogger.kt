package com.github.wigggy.botsbase.systems.bot_tools

import java.io.BufferedWriter
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ColorLogger(private val ownerName: String) {

    private val red = "\u001B[31m"
    private val blue = "\u001B[34m"
    private val cyan = "\u001B[36m"
    private val green = "\u001B[32m"
    private val purple = "\u001B[35m"
    private val reset = "\u001B[0m"
    private val bold = "\u001B[1m"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val writer: BufferedWriter = initBufferedWriter()
    private val lockWriter = Any()

    // Levels In Order
    // debug
    // info
    // warn
    private val curLevel = "i"


    private fun initBufferedWriter(): BufferedWriter {
        val f =
            if (ownerName.contains(".") == false) {
                "bot_tools_logger_" + ownerName + ".txt"
            }
            else {
                "bot_tools_logger_" + ownerName
            }

        val p = "_bot_logs\\$f"

        return BufferedWriter(FileWriter(p, true))
    }


    /** Performs thread-safe write to log file and output to console */
    private fun writeToFile(txt: String, lvl: String) {
        Thread {
            synchronized(lockWriter) {

                when (curLevel) {
                    "d" -> {
                        if (lvl == "d" || lvl == "i" || lvl == "w") {
                            println(txt)
                        }
                    }
                    "i" -> {
                        if (lvl == "i" || lvl == "w"){
                            println(txt)
                        }
                    }
                    "w" -> {
                        if (lvl == "w") {
                            println(txt)
                        }
                    }
                }

                writer.write(txt)
                writer.newLine()
                writer.flush()
            }
        }.start()
    }

    fun dbug(msg: String) {
        val dts = LocalDateTime.now().format(dateTimeFormatter)
        val s = "$bold$blue[$dts] [DBUG] [$ownerName] $msg $reset"
        writeToFile(s, "d")
    }

    fun info(msg: String){
        val dts = LocalDateTime.now().format(dateTimeFormatter)
        val s = "$bold$green[$dts] [INFO] [$ownerName] $msg $reset"
        writeToFile(s, "i")
    }

    fun warn(msg: String) {
        val dts = LocalDateTime.now().format(dateTimeFormatter)
        val s = "$bold$red[$dts] [WARN] [$ownerName] $msg $reset"
        writeToFile(s, "w")
    }


}