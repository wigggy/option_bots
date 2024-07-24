package com.github.wigggy.app.home.components.bot_panel

import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.common.doubleToDollarFormat
import com.github.wigggy.app.common.doubleToPercentFormat
import com.github.wigggy.app.common.doubleToTwoDecimalFormat
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Paint



class KeyValueHBoxColored(
    key: String,
    value: String,
    private val isPct: Boolean = false,
    private val isDollar: Boolean = false,
    private val isInt: Boolean = false
): HBox()  {

    private val keyLabel = buildKeyLabel(key)
    private val valueLabel = buildValueLabel()


    init {
        minWidth = AppValues.APP_WIDTH * .25
        setupStyle()
        maxWidth = minWidth
        prefWidth = minWidth

        val spacer = Region()
        this.children.add(keyLabel)
        this.children.add(spacer)
        this.children.add(valueLabel)

        // Spread key/val apart from each other
        this.apply {
            setHgrow(spacer, Priority.ALWAYS)
        }

        updateValue(value)
    }


    private fun setupStyle(){
        this.style = """
            -fx-padding: 2px 5px 2px 5px;
        """.trimIndent()
    }


    private fun buildKeyLabel(key: String): Label {
        return Label(key).apply {
            textFill = Paint.valueOf(AppValues.COLOR_NORMAL_TEXT)
        }
    }


    private fun buildValueLabel(): Label {
        return Label().apply {
            textFill = Paint.valueOf(AppValues.COLOR_NORMAL_TEXT)
        }
    }


    fun updateValue(value: String){
        // Int
        if (isInt){
            val i = value.toInt()
            if (i > 0) {
                valueLabel.text = i.toString()
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_GAIN_GREEN)
            }else if (i < 0){
                valueLabel.text = i.toString()
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_LOSS_RED)
            }else {
                valueLabel.text = i.toString()
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_NORMAL_TEXT)
            }
        }

        // Pct
        else if (isPct){
            val i = value.toDouble()
            if (i > 0.0){
                valueLabel.text = doubleToPercentFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_GAIN_GREEN)
            }else if (i < 0.0){
                valueLabel.text = doubleToPercentFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_LOSS_RED)
            }else {
                valueLabel.text = doubleToPercentFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_NORMAL_TEXT)
            }
        }

        // Dollar
        else if (isDollar) {
            val i = value.toDouble()
            if (i > 0.0){
                valueLabel.text = doubleToDollarFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_GAIN_GREEN)
            }else if (i < 0.0){
                valueLabel.text = doubleToDollarFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_LOSS_RED)
            }else {
                valueLabel.text = doubleToDollarFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_NORMAL_TEXT)
            }
        }

        // Plain Double
        else {
            val i = value.toDouble()
            if (i > 0.0){
                valueLabel.text = doubleToTwoDecimalFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_GAIN_GREEN)
            }else if (i < 0.0){
                valueLabel.text = doubleToTwoDecimalFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_LOSS_RED)
            }else {
                valueLabel.text = doubleToTwoDecimalFormat(i)
                valueLabel.textFill = Paint.valueOf(AppValues.COLOR_NORMAL_TEXT)
            }
        }

    }
}