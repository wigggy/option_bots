package com.github.wigggy.app.home

import com.github.wigggy.app.common.AppValues
import com.github.wigggy.app.common.Screen
import com.github.wigggy.app.common.camelCaseToNormalCase
import com.github.wigggy.app.common.collectLatestSafe
import com.github.wigggy.app.home.components.bot_panel.BotInfoPanel
import com.github.wigggy.app.home.components.open_pos_display.OpenPosDisplay
import com.github.wigggy.botsbase.systems.managers.BotManager
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


    // BotPanel Scrollpane
    private val scrollPaneBotPanels = buildScrolLPaneBotPanel()
    private val scrollPaneBotPanelContent = buildScrollPaneBotPanelContent()

    // Open Pos Display
    private val openPosDisplayContainerVBox = VBox()
    private val labelHeaderOpenPos = buildOpenPosHeader()
    private val scrollPaneOpenPos = buildScrollPaneOpenPos()
    private val scrollPaneOpenPosContentVBox = buildScrollPaneOpenPosContent()
    private val listOfOpenPosDisplays = mutableListOf<OpenPosDisplay>()

    init {
        initStyle()
        initUiStateFlowCollection()

        // BotPanel scrollpane
        scrollPaneBotPanels.content = scrollPaneBotPanelContent
        this.left = scrollPaneBotPanels

        // Open Position scrollpane
        scrollPaneOpenPos.content = scrollPaneOpenPosContentVBox
        openPosDisplayContainerVBox.children.add(labelHeaderOpenPos)
        openPosDisplayContainerVBox.children.add(scrollPaneOpenPos)
        this.center = openPosDisplayContainerVBox
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
                    coroutineScopeUiUpdates.launch {
                        bp.update(mapOfBotStates[bn]!!)
                    }
                }
            }


            // Update Open Pos Displays (Update previously created Displays, create more if needed) -------------------
            // Update Header
            val newHeader = camelCaseToNormalCase(uiState.curBotnameOpenPosShowing) + " Open Positions"
            labelHeaderOpenPos.text = newHeader

            // Sort list so the oldest open pos is on top
            val curBotsOpenPosList = uiState.curBotsOpenPosList.sortedBy { it.openTimestampMs }

            // Check how many panels exist, create more if needed
            val newPanelsNeeded = curBotsOpenPosList.size - listOfOpenPosDisplays.size
            if (newPanelsNeeded > 0){
                // Create panels, add to openPosDisplaysList
                for (n in 0..newPanelsNeeded){
                    listOfOpenPosDisplays.add(OpenPosDisplay())
                }
            }

            // Get OpenPosDisplays from list of created displays and update with new values
            val updatedDisplays = listOfOpenPosDisplays.take(curBotsOpenPosList.size)
            for (n in 0..curBotsOpenPosList.lastIndex){
                updatedDisplays[n].update(curBotsOpenPosList[n])
            }

            // Clear old disps and Add updated disps to ScrollPane Content Vbox
            coroutineScopeUiUpdates.launch {
                scrollPaneOpenPosContentVBox.children.clear()
                scrollPaneOpenPosContentVBox.children.addAll(updatedDisplays)
            }
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