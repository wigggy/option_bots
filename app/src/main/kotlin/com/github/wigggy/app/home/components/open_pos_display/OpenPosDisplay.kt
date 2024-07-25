package com.github.wigggy.app.home.components.open_pos_display

import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.common.doubleToDollarFormat
import com.github.wigggy.app.common.doubleToPercentFormat
import com.github.wigggy.app.common.timestampToHHMMAP_MMDDYYYY
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

class OpenPosDisplay() : VBox() {

    private val optionSymbolLabel = Label().apply {
        style = "-fx-font-size: 50; -fx-font-weight: bold"
        prefWidth = 790.0
        prefHeight = 30.0
        alignment = Pos.TOP_CENTER
    }

    private val posIdLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val quanLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val openDTLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val costPerLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val tpLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val totalCostLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val stockEntryPriceLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val curPosValueLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val stopLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val highestPctLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val stockPriceLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val lowestPctLabel = Label().apply {
        prefWidth = 150.0
        style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
        alignment = Pos.BOTTOM_CENTER
    }

    private val gainDollarLabel = Label().apply {
        prefHeight = 25.0
        prefWidth = 150.0
        alignment = Pos.TOP_CENTER
        font = Font.font("Arial", FontWeight.BOLD, 28.0)
    }

    private val gainPercentLabel = Label().apply {
        prefHeight = 25.0
        prefWidth = 150.0
        alignment = Pos.TOP_CENTER
        font = Font.font("Arial", FontWeight.BOLD, 28.0)


    }

    private val bottomSpacer = Region().apply {
        HBox.setHgrow(this, Priority.ALWAYS)
        style = "-fx-background-color: ${AppValues.COLOR_ACCENT_2};" // Set the color of the spacer
        // Optional: Set min, max, or preferred height/width if needed
        minHeight = 10.0
        minWidth = 0.0
    }

    init {
        prefHeight = 300.0
        maxHeight = 300.0
        minHeight = 300.0

        children.addAll(
            optionSymbolLabel,
            createHBox("  Pos ID#: ", posIdLabel, "  Quantity: ", quanLabel),
            createHBox("  Opened At: ", openDTLabel, "  Cost Per: ", costPerLabel),
            createHBox("  Take Profit: ", tpLabel, "  Total Cost: ", totalCostLabel),
            createHBox("  Entry Stock Price: ", stockEntryPriceLabel, "  Current Value: ", curPosValueLabel),
            createHBox("  Stop: ", stopLabel, "  Highest % Gain: ", highestPctLabel),
            createHBox("  Stock Price: ", stockPriceLabel, "  Lowest % Gain: ", lowestPctLabel),
            createHBox("", gainDollarLabel, "", gainPercentLabel),
            bottomSpacer
        )
        setStyle()
    }

    private fun setStyle() {
        this.style = """
            -fx-background-color: ${AppValues.COLOR_ACCENT_1}
        """.trimIndent()
    }

    private fun createHBox(label1Text: String, label1: Label, label2Text: String, label2: Label): HBox {
        val spacer = Label("").apply { prefWidth = 200.0 }
        return HBox().apply {
            children.addAll(
                Label(label1Text).apply {
                    prefWidth = 150.0
                    style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
                },
                label1,
                spacer,
                Label(label2Text).apply {
                    prefWidth = 150.0
                    style = "-fx-text-fill: antiquewhite; -fx-font-size: 16"
                },
                label2
            )
            style = """
                -fx-background-color: ${AppValues.COLOR_ACCENT_1};
            """.trimIndent()
        }
    }


    fun update(pos: OptionPosition){

        // Header -- Option Symbol
        val GlColor = if (pos.gainLossDollarTotal > 0.0) {
            AppValues.COLOR_GAIN_GREEN
        } else if (pos.gainLossDollarTotal < 0.0){
            AppValues.COLOR_LOSS_RED
        } else {
            "antiquewhite"
        }
        val putCallColor = if (pos.optionSymbol[12] == 'C') {
            AppValues.COLOR_GAIN_GREEN
        } else {
            AppValues.COLOR_LOSS_RED
        }
        optionSymbolLabel.textFill = Paint.valueOf(putCallColor)
        optionSymbolLabel.text = pos.optionSymbol


        // Left Side
        posIdLabel.text = pos.id.toString()
        openDTLabel.text = timestampToHHMMAP_MMDDYYYY(pos.openTimestampMs)
        val tp = if (pos.takeProfitDollarTarget != 0.0) "$${pos.takeProfitDollarTarget}" else "%${pos.takeProfitPercentTarget}"
        val stp = if (pos.stopLossDollarTarget != 0.0) "$${pos.stopLossDollarTarget}" else "%${pos.stopLossPercentTarget}"
        tpLabel.text = tp
        stockEntryPriceLabel.text = doubleToDollarFormat(pos.underlyingPriceAtPurchase)
        stopLabel.text = stp
        stockPriceLabel.text = doubleToDollarFormat(pos.underlyingPriceCurrent)
        gainDollarLabel.text = doubleToDollarFormat(pos.gainLossDollarTotal)
        gainDollarLabel.textFill = Paint.valueOf(GlColor)

        // Right Side
        quanLabel.text = pos.quantity.toString()
        costPerLabel.text = doubleToDollarFormat(pos.pricePer)
        totalCostLabel.text = doubleToDollarFormat(pos.totalPrice)
        curPosValueLabel.text = doubleToDollarFormat(pos.curValueOfPosition)
        highestPctLabel.text = doubleToPercentFormat(pos.highestGainPercent)
        lowestPctLabel.text = doubleToPercentFormat(pos.lowestGainPercent)
        gainPercentLabel.text = doubleToPercentFormat(pos.gainLossPercent)
        gainPercentLabel.textFill = Paint.valueOf(GlColor)
    }
}