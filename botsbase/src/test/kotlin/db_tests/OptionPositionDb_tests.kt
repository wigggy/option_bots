package db_tests

import com.github.wigggy.botsbase.systems.data.OptionPositionDb
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import java.util.*


fun main() {

    val db = OptionPositionDb("DB_TEST")

    // C
    val pos = createTestOptionPosition()
    val create = db.insertOptionPosition(pos)
    println("Create: $create")

    // R
    val read = db.getOptionPositionByID(pos.id)
    val readSuccess = read != null
    println("Read: $readSuccess")

    // U
    val test = "TEST SUCCESS"
    val newPos = read?.copy(closeReason = test)
    val update = db.updateOptionPosition(newPos!!)
    val newRead = db.getOptionPositionByID(newPos.id)
    val updateSuccess = newRead?.closeReason == test
    println("Update Return: $update. Update Actual Check: $updateSuccess")

    // D
    val del = db.deleteOptionPosition(newPos)
    val g = db.getOptionPositionByID(newPos.id)
    val delSuccess = g == null
    println("Del Return: $del  Del Check: $delSuccess")

}


private fun createTestOptionPosition(): OptionPosition {
    val id = UUID.randomUUID()
    val now = System.currentTimeMillis()
    val date = Date(now)
    return OptionPosition(
        id = id,
        botName = "TestBot",
        botDesc = "Test Bot Description",
        stockSymbol = "AAPL",
        optionSymbol = "AAPL210917C00145000",
        lastUpdatedTimestampMs = now,
        lastUpdatedDate = date,
        openTimestampMs = now,
        openDate = date,
        closeTimestampMs = 0L,
        closeDate = Date(0L),
        isPaperTrade = true,
        putCall = "CALL",
        strikePrice = 145.0,
        lastTradingDay = now,
        expirationDate = "2023-09-17",
        description = "Test option position",
        dteAtPurchaseTime = 30,
        quantity = 10,
        fees = 0.65,
        pricePer = 1.0,
        totalPrice = 10.0,
        bid = 1.0,
        ask = 1.2,
        mark = 1.1,
        highPrice = 1.5,
        lowPrice = 0.9,
        openPrice = 1.0,
        totalVolume = 100,
        daysPercentChangeAtPurchaseTime = 1.5,
        daysNetChangeAtPurchaseTime = 0.1,
        volatility = 20.0,
        delta = 0.5,
        gamma = 0.1,
        theta = -0.05,
        vega = 0.2,
        rho = 0.01,
        openInterest = 500,
        timeValue = 1.0,
        theoreticalOptionValue = 1.1,
        dte = 30,
        intrinsicValue = 0.0,
        high52Week = 150.0,
        low52Week = 100.0,
        inTheMoney = false,
        itmDistance = 5.0,
        gainLossDollarTotal = 0.0,
        gainLossDollarPer = 0.0,
        gainLossPercent = 0.0,
        takeProfitDollarTarget = 20.0,
        takeProfitPercentTarget = 100.0,
        stopLossDollarTarget = -5.0,
        stopLossPercentTarget = -50.0,
        closeReason = "None",
        quoteAtOpenJson = "{}",
        quoteAtCloseJson = "{}",
        underlyingPriceCurrent = 145.0,
        underlyingPriceAtPurchase = 145.0,
        curValuePerContract = 1.0,
        curValueOfPosition = 10.0,
        highestGainDollarPerContract = 2.0,
        highestGainDollarTotal = 20.0,
        lowestGainDollarPerContract = -1.0,
        lowestGainDollarPerTotal = -10.0,
        highestGainPercent = 100.0,
        lowestGainPercent = -50.0,
        extraData = mapOf("testKey" to "testValue")
    )
}
