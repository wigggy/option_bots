package com.github.wigggy.app.home.components.bot_panel

import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.common.camelCaseToNormalCase
import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.paint.Paint

class BotPanel(botstate: BotState): VBox() {

    private var botName = botstate.botName

    // Ui Components
    private val botNameLabel: Label
    private val daysGain: KeyValueHBoxColored
    private val nPosOpened: KeyValueHBox
    private val nWins: KeyValueHBox
    private val nLosses: KeyValueHBox
    init {
        styleSetup()
        this.alignment = Pos.CENTER
        // Name
        botNameLabel = buildBotNameLabel()
        this.children.add(botNameLabel)

        // daysGain
        daysGain = KeyValueHBoxColored("Days Gain: ", botstate.gainLossDollarDaysRealized.toString(),
            isDollar = true)
        this.children.add(daysGain)

        // nPosOpened
        nPosOpened = KeyValueHBox("Positions Opened: ", botstate.nPositionsOpenedToday.toString(),
            isInt = true)
        this.children.add(nPosOpened)

        nWins = KeyValueHBox("Wins: ", botstate.daysWins.toString(), isInt = true)
        this.children.add(nWins)

        nLosses = KeyValueHBox("Losses: ", botstate.daysLosses.toString(), isInt = true)
        this.children.add(nLosses)
    }

    fun styleSetup() {
        this.style = """
           -fx-background-color: ${AppValues.COLOR_ACCENT_1};
           -fx-padding: 5px 0px 5px 0px;
        """.trimIndent()
    }

    fun getBotName(): String {
        return botName
    }


    fun update(botstate: BotState) {
        daysGain.updateValue(botstate.gainLossDollarDaysRealized.toString())
        nPosOpened.updateValue(botstate.nPositionsOpenedToday.toString())
    }


    private fun buildBotNameLabel(): Label {
        return Label(camelCaseToNormalCase(botName)).apply {
            textFill = Paint.valueOf(AppValues.COLOR_NORMAL_TEXT)
            style = """
                -fx-text-fill: ${AppValues.COLOR_NORMAL_TEXT};
                -fx-background-color: ${AppValues.COLOR_ACCENT_2};
                -fx-font-weight: bold;
                -fx-font-size: 20px;
                -fx-padding: 5px;
                -fx-background-radius: 7px;
            """.trimIndent()
            prefWidth = 170.0
            minWidth = 170.0
            maxWidth = 170.0
            alignment = Pos.CENTER
        }
    }
}