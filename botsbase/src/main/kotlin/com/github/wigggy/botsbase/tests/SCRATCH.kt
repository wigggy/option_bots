import com.github.wigggy.botsbase.systems.analysis.TechnicalAnalysis
import com.github.wigggy.botsbase.systems.bot_tools.Common


fun main() {
    val cs = Common.csApi


    val testChart = cs.getHistoricData1day("SPY", 6, true)!!

    val testValues = testChart.close
    val indValues = TechnicalAnalysis.rsi(testValues, 4).rsiValues

    println("Indicator Size: ${indValues.size}")
    println("TestValues Size: ${testValues.size}")

    println()
    println("Indicator: ${indValues}")
    println("Test Vals: ${testValues}")
}
