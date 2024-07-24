package com.github.wigggy.app.home

import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.common.Screen
import com.github.wigggy.app.common.camelCaseToNormalCase
import com.github.wigggy.app.common.collectLatestSafe
import com.github.wigggy.app.home.components.bot_panel.BotInfoPanel
import com.github.wigggy.app.home.components.open_pos_display.OpenPosDisplay
import com.github.wigggy.botsbase.systems.BotManager
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.*


// Maybe use GridPane() instead
class HomeScreen(vm: HomeViewModel, extraData: Map<String, String>? = null): Screen, BorderPane(){

    // Set up like this so viewmodel reference can be cleared in onDestroy()
    private var _viewModel: HomeViewModel? = vm
    private val viewModel get() = _viewModel!!

    private val coroutineScopeUiUpdates = CoroutineScope(Job() + Dispatchers.Main)

    private val l = Label("Home")

    // BotPanel Scrollpane
    private val scrollPaneBotPanels = buildScrolLPaneBotPanel()
    private val scrollPaneBotPanelContent = buildScrollPaneBotPanelContent()

    // Open Pos
    private val vboxOpenPos = VBox()
    private val labelOpenPos = buildOpenPosHeader()
    private val scrollPaneOpenPos = buildScrollPaneOpenPos()
    private val scrollPaneOpenPosContent = buildScrollPaneOpenPosContent()

    init {
        initStyle()
        initUiStateFlowCollection()
        // header
        this.top = l

        // BotPanel scrollpane
        scrollPaneBotPanels.content = scrollPaneBotPanelContent
        this.left = scrollPaneBotPanels

        scrollPaneOpenPos.content = scrollPaneOpenPosContent
        vboxOpenPos.children.add(labelOpenPos)
        vboxOpenPos.children.add(scrollPaneOpenPos)
        this.center = vboxOpenPos

    }


    private fun initStyle(){
        this.style = """
            -fx-background-color: ${AppValues.COLOR_FOR_BACKGROUND};
        """.trimIndent()
    }


    private fun buildScrolLPaneBotPanel(): ScrollPane {

        // ScrollPane that will be filled with BotPanels
        val sp = ScrollPane()
        sp.minHeight = AppValues.APP_HEIGHT * .60
        sp.prefHeight = AppValues.APP_HEIGHT * .60

        // Load the external CSS file
        val cssPath = "/css_style_sheets/botpanel_scrollpane_style.css"
        sp.stylesheets.add(javaClass.getResource(cssPath)!!.toExternalForm())//


        // Set the scroll bars to always be visible
        sp.vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS

        return sp
    }


    private fun buildScrollPaneBotPanelContent(): VBox {
        val vb = VBox()
        vb.style = """
            -fx-background-color: ${AppValues.COLOR_FOR_BACKGROUND};
        """.trimIndent()
        vb.alignment = Pos.CENTER
        return vb
    }


    private fun buildOpenPosHeader(): Label{

        val label = Label("Open Positions").apply {
            style = """
                -fx-text-fill: ${AppValues.COLOR_NORMAL_TEXT}
            """.trimIndent()
        }
        return label
    }


    private fun buildScrollPaneOpenPos(): ScrollPane {
        val sp = ScrollPane()

        return sp
    }


    private fun buildScrollPaneOpenPosContent(): VBox {
        val vb = VBox()

        return vb
    }


    private fun initUiStateFlowCollection() {

        viewModel.uiState.collectLatestSafe(this, coroutineScopeUiUpdates){ uiState ->

            // Update Bot Info Panels   -------------------------------------------------------------------------------
            val mapOfBotStates = uiState.botStates
            // Create new panels if none are there
            if (scrollPaneBotPanelContent.children.isEmpty()){
                // Create panels
                val listOfPanels = mutableListOf<BotInfoPanel>()
                for (botname in mapOfBotStates.keys) {
                    val panel = BotInfoPanel(
                        bs = mapOfBotStates[botname]!!,
                        onPosButtonClicked = { viewModel.changeOpenPosList(botname)},
                        onPowerButtonClicked = { BotManager.turnBotOn(botname) },
                        onSettingsButtonClicked = { viewModel.navigateToSettings(botname)})   // Never null if key is present
                    listOfPanels.add(panel)
                }

                // Note scroll value
                val verticalScroll = scrollPaneBotPanels.vvalue

                // Update the UI on the JavaFX Application Thread
                // Todo try using coroutineScope.launch(Dispatchers.Main)
                coroutineScopeUiUpdates.launch {
                    scrollPaneBotPanelContent.children.setAll(listOfPanels)
                    scrollPaneBotPanels.vvalue = verticalScroll
                }
            }
            // Update cur panels w new data
            else {
                val curPanels = scrollPaneBotPanelContent.children.toList()
                for (panel in curPanels){
                    val bp = panel as BotInfoPanel
                    val bn = bp.initialBotstate.botName
                    Platform.runLater {
                        bp.update(mapOfBotStates[bn]!!)
                    }
                }
            }

            // TODO Make this more efficient by checking if the same bot's pos disp is showing and if so
            //      updating open pos disps that already exist and clearing open pos disps if pos has been closed
            //      or adding more open pos disps new positions have been opened.
            // Update Open Pos Display --------------------------------------------------------------------------------
            val newHeader = camelCaseToNormalCase(uiState.curBotnameOpenPosShowing) + " Open Positions"
            labelOpenPos.text = newHeader

            // Make new disp
            val posDisps = mutableListOf<OpenPosDisplay>()
            for (p in uiState.curBotsOpenPosList){
                posDisps.add(OpenPosDisplay(p))
            }

            coroutineScopeUiUpdates.launch{
                val scrollValueV = scrollPaneOpenPos.vvalue
                val scrollValueH = scrollPaneOpenPos.hvalue

                scrollPaneOpenPosContent.children.clear()
                scrollPaneOpenPosContent.children.addAll(posDisps)
                scrollPaneOpenPos.vvalue = scrollValueV
                scrollPaneOpenPos.hvalue = scrollValueH
            }







            // Update Open Pos Display --------------------------------------------------------------------------------

        }
    }


    override fun destroy() {
        coroutineScopeUiUpdates.cancel()
        _viewModel?.destroy()
        _viewModel = null
    }

    companion object {
        const val SCREEN_NAME = "HOME"
    }
}

fun main() {
}