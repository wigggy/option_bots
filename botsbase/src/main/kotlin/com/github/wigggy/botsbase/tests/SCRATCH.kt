import com.github.wigggy.botsbase.systems.analysis.TechnicalAnalysis
import com.github.wigggy.botsbase.systems.bot_tools.Common


fun main() {
    val x = Common.csApi.getHistoricData5min("SPY", 5, false)
    println("\u001B[35mHello")
}
