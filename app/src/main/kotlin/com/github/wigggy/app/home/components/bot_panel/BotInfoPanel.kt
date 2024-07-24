package com.github.wigggy.app.home.components.bot_panel

import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.common.doubleToDollarFormat
import com.github.wigggy.app.common.doubleToPercentFormat
import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font

class BotInfoPanel(
    bs: BotState,
    onPosButtonClicked: (botname: String) -> Unit,
    onPowerButtonClicked: (botname: String) -> Unit,
    onSettingsButtonClicked: (botname: String) -> Unit

) : VBox() {
    val initialBotstate = bs
    private val botNameLabel: Label
    private val daysGainLabel: Label
    private val positionsOpenedLabel: Label
    private val winsLabel: Label
    private val lossesLabel: Label
    private val winPctLabel: Label
    private val biggestWinLabel: Label
    private val biggestLossLabel: Label
    private val topGainingTickerLabel: Label
    private val topTickersGainLabel: Label
    private val avgGainLabel: Label
    private val avgLossLabel: Label
    private val cycleCountLabel: Label
    private val powerButton: Button
    private val botSettingsButton: Button
    private val positionsButton: Button

    init {
        // Set VBox properties
        prefWidth = 203.0
        prefHeight = 275.0
        style = "-fx-background-color: #1F2038; -fx-border-color: #171E2A; -fx-border-width: 2px 2px 4px 2px"

        // Initialize labels and buttons
        botNameLabel = Label("Bot Name Here").apply {
            textFill = javafx.scene.paint.Color.ANTIQUEWHITE
            style = "-fx-background-color: #2D3851; -fx-border-color: #1F2038; -fx-border-width: 2"
            prefWidth = 200.0
            alignment = Pos.TOP_CENTER
            font = Font.font("Times New Roman", 16.0)
        }

        daysGainLabel = createStyledLabel()
        positionsOpenedLabel = createStyledLabel()
        winsLabel = createStyledLabel()
        lossesLabel = createStyledLabel()
        winPctLabel = createStyledLabel()
        biggestWinLabel = createStyledLabel()
        biggestLossLabel = createStyledLabel()
        topGainingTickerLabel = createStyledLabel()
        topTickersGainLabel = createStyledLabel()
        avgGainLabel = createStyledLabel()
        avgLossLabel = createStyledLabel()
        cycleCountLabel = createStyledLabel(fontSize = 18.0, fontWeightBold = true)

        val powerColor = if (bs.power) AppValues.COLOR_GAIN_GREEN else AppValues.COLOR_LOSS_RED

        powerButton = Button("Power").apply {
            prefWidth = 150.0
            prefHeight = 25.0
            style = "-fx-background-color: $powerColor;"
            setOnAction { onPowerButtonClicked(initialBotstate.botName) }
        }

        botSettingsButton = Button("Settings").apply {
            prefWidth = 150.0
            prefHeight = 25.0
            style = "-fx-background-color:#2D3851; -fx-text-fill: white; -fx-font-weight: bold"
            setOnAction { onSettingsButtonClicked(initialBotstate.botName) }
        }

        positionsButton = Button("Positions").apply {
            prefWidth = 150.0
            prefHeight = 25.0
            style = "-fx-background-color:#2D3851; -fx-text-fill: white; -fx-font-weight: bold"
            setOnAction { onPosButtonClicked(initialBotstate.botName) }
        }

        // Add children to VBox
        children.addAll(
            botNameLabel,
            createHBox(" Days Gain: ", daysGainLabel),
            createHBox(" Positions Opened: ", positionsOpenedLabel),
            createHBox(" Wins: ", winsLabel),
            createHBox(" Losses: ", lossesLabel),
            createHBox(" Win %: ", winPctLabel),
            createHBox(" Biggest Win: ", biggestWinLabel),
            createHBox(" Biggest Loss: ", biggestLossLabel),
            createHBox(" Top Ticker: ", topGainingTickerLabel),
            createHBox(" Top Tickers Gain: ", topTickersGainLabel),
            createHBox(" Avg Gain: ", avgGainLabel),
            createHBox(" Avg Loss: ", avgLossLabel),
            VBox().apply {
                children.addAll(
                    Label(" Cycle Count ").apply {
                        prefHeight = 20.0
                        prefWidth = 200.0
                        style = "-fx-text-fill: antiquewhite; -fx-font-weight: bold; -fx-font-size: 18"
                        alignment = Pos.TOP_CENTER
                    },
                    cycleCountLabel
                )
            },
            Label().apply { prefHeight = 5.0; style = "-fx-font-size: 5" },
            VBox().apply {
                spacing = 2.0
                alignment = Pos.TOP_CENTER
                children.addAll(
                    powerButton,
                    botSettingsButton,
                    positionsButton
                )
            },
            Label().apply { prefHeight = 10.0 }
        )
        update(bs)
    }

    private fun createStyledLabel(fontSize: Double = 16.0, fontWeightBold: Boolean = false): Label {
        return Label().apply {
            textFill = javafx.scene.paint.Color.ANTIQUEWHITE
            prefHeight = 20.0
            if (fontWeightBold) {
                style = "-fx-font-size: $fontSize; -fx-font-weight: bold"
            } else {
                style = "-fx-font-size: $fontSize"
            }
        }
    }

    private fun createHBox(labelText: String, label: Label): HBox {
        return HBox().apply {
            children.addAll(
                Label(labelText).apply {
                    prefWidth = 140.0
                    prefHeight = 20.0
                    style = "-fx-text-fill: antiquewhite"
                },
                label
            )
        }
    }

    fun update(bs: BotState){
        botNameLabel.text = bs.botName
        daysGainLabel.text = doubleToDollarFormat(bs.gainLossDollarDaysRealized)        // TODO color
        positionsOpenedLabel.text = bs.nPositionsOpenedToday.toString()
        winsLabel.text = bs.daysWins.toString()
        lossesLabel.text = bs.daysLosses.toString()
        winPctLabel.text = doubleToPercentFormat(bs.daysWinPercentage)
        biggestWinLabel.text = doubleToDollarFormat(bs.daysBiggestGain)
        biggestLossLabel.text = doubleToDollarFormat(bs.daysBiggestLoss)
        topGainingTickerLabel.text = bs.topGainingTicker
        topTickersGainLabel.text = doubleToDollarFormat(bs.topGainingTickerGainDollar)
        avgGainLabel.text = doubleToDollarFormat(bs.avgWinAmount)
        avgLossLabel.text = doubleToDollarFormat(bs.avgLossAmount)
        cycleCountLabel.text = bs.cycleCount.toString()
    }
}
