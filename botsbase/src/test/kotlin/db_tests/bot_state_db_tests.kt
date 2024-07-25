package db_tests

import com.github.wigggy.botsbase.systems.data.BotStateDb
import com.github.wigggy.botsbase.systems.data.data_objs.BotState


fun main() {
    botStateCreateTest()
    botStateReadTest()
    botStateUpdateTest()
    botStateDeleteTest()
}


fun botStateCreateTest() {
    val bn = "bot state create test"
    val bs = BotState(botName = "bot state create test")

    // insert
    val sm = BotStateDb(bn)

    sm.insertBotState(bs)

    // get
    val get = sm.getBotState()

    val test = if (get != null) true else false

    sm.deleteBotState()

    println("Create: $test")
}


fun botStateReadTest() {
    val bn = "bot state create test"
    val bs = BotState(botName = "bot state create test")

    // insert
    val sm = BotStateDb(bn)

    sm.insertBotState(bs)

    // get
    val get = sm.getBotState()

    val test = if (get != null) true else false

    sm.deleteBotState()

    println("Read: $test")
}


fun botStateUpdateTest() {
    val bn = "bot state create test"
    val bs = BotState(botName = "bot state create test")

    // insert
    val sm = BotStateDb(bn)

    sm.insertBotState(bs)

    // get
    val get = sm.getBotState()

    // update
    sm.updateBotState(get!!.copy(botDesc = "UPDATE WORKS"))

    val updated = sm.getBotState()

    val test = if (updated?.botDesc == "UPDATE WORKS") true else false

    sm.deleteBotState()

    println("Update: $test")
}


fun botStateDeleteTest() {
    val bn = "bot state create test"
    val bs = BotState(botName = "bot state create test")

    // insert
    val sm = BotStateDb(bn)

    sm.insertBotState(bs)
    sm.deleteBotState()

    // get
    val get = sm.getBotState()

    val test = if (get == null) true else false


    println("Delete: $test")
}

