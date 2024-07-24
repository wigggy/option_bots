//package com.github.wi110r.botsbase.systems
//
//import com.github.wi110r.botsbase.systems.bot_tools.BotToolsLogger
//import com.github.wi110r.botsbase.systems.bot_tools.Common
//import com.github.wi110r.botsbase.systems.bot_tools.MarketTimeUtil
//import com.github.wi110r.botsbase.systems.data.data_objs.OptionPosition
//import com.github.wi110r.botsbase.tools.Log
//import com.github.wi110r.com.github.wi110r.charlesschwab_api.data_objs.OptionQuote
//import com.google.gson.reflect.TypeToken
//import com.zaxxer.hikari.HikariConfig
//import com.zaxxer.hikari.HikariDataSource
//import java.sql.*
//import java.sql.Date
//import java.time.LocalDate
//import java.time.ZoneId
//import java.util.*
//import java.util.concurrent.atomic.AtomicBoolean
//
//
//object PositionsManager {
//
//    private val log = BotToolsLogger("PosManager")
//    private val jdbcUrl = "jdbc:sqlite:_savedata\\PosManager.db"
//    private val OPTION_POS_TABLE_NAME = "option_positions"
//    private var openPosUpdaterThread: Thread? = null
//    private val openPosUpdaterThreadShutdownFlag = AtomicBoolean(false)
//    private val dbDatasource: HikariDataSource = buildHikariDatasource()
//    private val lockForWrites = Any()
//
//
//    init {
//        createOptionPositionTable()
//    }
//
//
//    fun buyOrderMock(
//        botName: String,
//        bot_desc: String,
//        option_symbol: String,
//        quantity: Int,
//        tp_dollar: Double,
//        tp_pct: Double,
//        stop_dollar: Double,
//        stop_pct: Double,
//        extra_data: Map<String, String>
//    ): OptionPosition? {
//
//        val getExpDate = { quote: OptionQuote ->
//
//            val nums = quote.symbol.split(" ").last()
//            val d = nums.substring(0, 6)
//            val y = d.substring(0, 2)
//            val m = d.substring(2, 4)
//            val da = d.substring(4, 6)
//            val date = "$m-$da-$y"
//            date
//        }
//
//        val quote = Common.csApi.getOptionQuote(option_symbol) ?: return null
//        val putOrCall = isPutOrCall(quote.symbol)
//        val curTime = System.currentTimeMillis()
//        val newPos = OptionPosition(
//            id = UUID.randomUUID(),
//            botName = botName,
//            botDesc = bot_desc,
//            stockSymbol = quote.symbol.split(" ").first(),
//            optionSymbol = quote.symbol,
//            lastUpdatedTimestampMs = curTime,
//            lastUpdatedDate = java.util.Date(curTime),
//            openTimestampMs = curTime,
//            openDate = java.util.Date(curTime),
//            closeTimestampMs = 0,
//            closeDate = java.util.Date(0),
//            isPaperTrade = true,
//            putCall = putOrCall,
//            strikePrice = quote.strikePrice,
//            lastTradingDay = quote.lastTradingDay,
//            expirationDate = getExpDate(quote),
//            description = quote.description,
//            dteAtPurchaseTime = quote.daysToExpiration,
//            quantity = quantity,
//            fees = quantity.toDouble() * 1.3,
//            pricePer = quote.askPrice + 1.3,
//            totalPrice = (quote.askPrice * quantity.toDouble()) + (quantity.toDouble() * 1.3),
//            bid = quote.bidPrice,
//            ask = quote.askPrice,
//            mark = quote.mark,
//            highPrice = quote.highPrice,
//            lowPrice = quote.lowPrice,
//            openPrice = quote.openPrice,
//            totalVolume = quote.totalVolume,
//            daysPercentChangeAtPurchaseTime = quote.markPercentChange,
//            daysNetChangeAtPurchaseTime = quote.netPercentChange,
//            volatility = quote.volatility,
//            delta = quote.delta,
//            gamma = quote.gamma,
//            theta = quote.theta,
//            vega = quote.vega,
//            rho = quote.rho,
//            openInterest = quote.openInterest,
//            timeValue = quote.timeValue,
//            theoreticalOptionValue = quote.theoreticalOptionValue,
//            dte = quote.daysToExpiration,
//            intrinsicValue = quote.moneyIntrinsicValue,
//            high52Week = quote.weekHigh52,
//            low52Week = quote.weekLow52,
//            inTheMoney = itmCheck(putOrCall, quote.strikePrice, quote.underlyingPrice),
//            itmDistance = itmDistanceCheck(putOrCall, quote.strikePrice, quote.underlyingPrice),
//            gainLossDollarTotal = 0.0,
//            gainLossDollarPer = 0.0,
//            gainLossPercent = 0.0,
//            takeProfitDollarTarget = tp_dollar,
//            takeProfitPercentTarget = tp_pct,
//            stopLossDollarTarget = stop_dollar,
//            stopLossPercentTarget = stop_pct,
//            closeReason = "POSITION IS OPEN",
//            quoteAtOpenJson = Common.gson.toJson(quote),
//            quoteAtCloseJson = "",
//            underlyingPriceCurrent = quote.underlyingPrice,
//            underlyingPriceAtPurchase = quote.underlyingPrice,
//            curValuePerContract = quote.bidPrice - .65,
//            curValueOfPosition = (quote.bidPrice * quantity.toDouble()) + (.65 * quantity.toDouble()),
//            highestGainDollarPerContract = 0.0,
//            highestGainDollarTotal = 0.0,
//            lowestGainDollarPerContract = 0.0,
//            lowestGainDollarPerTotal = 0.0,
//            highestGainPercent = 0.0,
//            lowestGainPercent = 0.0,
//            extraData = extra_data
//        )
//
//        // add to db
//        insertOptionPositionToDB(newPos)
//        return newPos
//    }
//
//
//    fun sellOrderMock(
//        optionPos: OptionPosition,
//        closeReason: String,
//        extraClosingData: String = "NONE"
//    ): OptionPosition? {
//
//        // NOTE ALL FEES ARE ADDED IN BUY
//        //      BOTH BUY AND SELL FEES ARE ADDED IN THE BUY FUNCTION
//
//        // get quote
//        val q = Common.csApi.getOptionQuote(optionPos.optionSymbol) ?: return null
//
//        // Add extra data if any
//        val extraData = optionPos.extraData.toMutableMap()
//        extraData["extraClosingData"] = extraClosingData
//
//        // Update pos dollar/pct values
//        val totalCost = optionPos.totalPrice
//        val quan = optionPos.quantity
//        val gainDollarTotal = (q.bidPrice * quan.toDouble()) - totalCost
//        val gainDollarPer = q.bidPrice - optionPos.pricePer
//        val gainPercent = (((q.bidPrice) - optionPos.pricePer) / optionPos.pricePer) * 100
//        val closeTs = System.currentTimeMillis()
//        val closeD = java.util.Date(closeTs)
//
//        val valOfPos = (q.bidPrice * quan.toDouble()) - optionPos.fees
//        val valPer = valOfPos / quan.toDouble()
//
//        val highGdPerCon =
//            if (gainDollarPer > optionPos.highestGainDollarPerContract) gainDollarPer
//            else optionPos.highestGainDollarPerContract
//        val highGdTotal =
//            if (gainDollarTotal > optionPos.highestGainDollarTotal) gainDollarTotal
//            else optionPos.highestGainDollarTotal
//
//        val lowestGdPerCon =
//            if (gainDollarPer < optionPos.lowestGainDollarPerContract) gainDollarPer
//            else optionPos.lowestGainDollarPerContract
//        val lowestGdTotal =
//            if (gainDollarTotal < optionPos.lowestGainDollarPerTotal) gainDollarTotal
//            else optionPos.lowestGainDollarPerTotal
//
//        val highestGPct =
//            if (gainPercent > optionPos.highestGainPercent) gainPercent
//            else optionPos.highestGainPercent
//        val lowGPct =
//            if (gainPercent < optionPos.lowestGainPercent) gainPercent
//            else optionPos.lowestGainPercent
//
//        val cpos = optionPos.copy(
//            lastUpdatedTimestampMs = closeTs,
//            lastUpdatedDate = java.util.Date(closeTs),
//            gainLossDollarTotal = gainDollarTotal,
//            gainLossDollarPer = gainDollarPer,
//            gainLossPercent = gainPercent,
//            closeTimestampMs = closeTs,
//            closeDate = closeD,
//            closeReason = closeReason,
//            quoteAtCloseJson = Common.gson.toJson(q),
//            underlyingPriceCurrent = q.underlyingPrice,
//            curValuePerContract = valPer,
//            curValueOfPosition = valOfPos,
//            bid = q.bidPrice,
//            ask = q.askPrice,
//            mark = q.mark,
//            highPrice = q.highPrice,
//            lowPrice = q.lowPrice,
//            totalVolume = q.totalVolume,
//            volatility = q.volatility,
//            delta = q.delta,
//            gamma = q.gamma,
//            theta = q.theta,
//            vega = q.vega,
//            rho = q.rho,
//            openInterest = q.openInterest,
//            timeValue = q.timeValue,
//            theoreticalOptionValue = q.theoreticalOptionValue,
//            dte = q.daysToExpiration,
//            intrinsicValue = q.moneyIntrinsicValue,
//            high52Week = q.weekHigh52,
//            low52Week = q.weekLow52,
//            inTheMoney = itmCheck(isPutOrCall(q.symbol), q.strikePrice, q.underlyingPrice),
//            itmDistance = itmDistanceCheck(isPutOrCall(q.symbol), q.strikePrice, q.underlyingPrice),
//            highestGainDollarPerContract = highGdPerCon,
//            highestGainDollarTotal = highGdTotal,
//            highestGainPercent = highestGPct,
//            lowestGainDollarPerContract = lowestGdPerCon,
//            lowestGainDollarPerTotal = lowestGdTotal,
//            lowestGainPercent = lowGPct
//        )
//
//        // Update pos in database and return
//        updateOptionPositionEntryToDB(cpos)
//        return cpos
//    }
//
//
//    fun shutdownOpenPositionsUpdateThread() {
//        // Shutdown pos update thread
//        try {
//            log.w("shutdownOpenPositionsUpdateThread() Shutdown Triggered")
//
//            if (openPosUpdaterThread != null){
//                openPosUpdaterThreadShutdownFlag.set(true)
//                openPosUpdaterThread?.interrupt()
//                openPosUpdaterThread!!.join(1000)
//                log.w("shutdownOpenPositionsUpdateThread() Shutdown Success")
//            }
//        } catch (e: Exception){
//            log.w("Failed with Exception to shutdown and join 'openPosUpdaterThread'.\n" +
//                    "${e.message}\n${e.stackTrace}")
//        }
//    }
//
//
//    fun startOpenPositionsUpdateThread(cycleTime: Long = 5_000) {
//        openPosUpdaterThread = Thread {
//
//            // Add shutdown hook for when program ends
//            Runtime.getRuntime().addShutdownHook(
//                Thread {
//                    log.w("Shutting Down Open Positions Update Thread...")
//
//                    // Interrupt Updater Thread and sleep giving time to finish cur update
//                    shutdownOpenPositionsUpdateThread()
//                }
//            )
//
//            var initialUpdate = false
//            // Start loop, checking for interrupt each loop
//            while (openPosUpdaterThreadShutdownFlag.get() == false) {
//
//                // Update once regardless of market open
//                if (!initialUpdate) {
//                    // Attempt update check success, set flag if true
//                    val success = updateAllOpenPositionsWithQuoteData()
//                    if (success) {
//                        initialUpdate = true
//                    }
//                    // If update failed, sleep 3s and retry
//                    else {
//                        Thread.sleep(3000L)
//                        continue
//                    }
//                }
//
//                // Check if market is open, wait if not
//                if (!MarketTimeUtil.isMarketOpen()) {
//                    val waitTime = MarketTimeUtil.getMarketWaitTimeInMillis()
//                    try { Thread.sleep(waitTime) }
//                    catch (e: Exception) { log.w("openPosUpdaterThread Sleep Interrupted") }                }
//
//                val success = updateAllOpenPositionsWithQuoteData()
//                if (!success){
//                    log.w("OPEN POSITION UPDATE FAILED. Will retry immediately.")
//                    Thread.sleep(1000L)
//                    continue
//                }
//                // Note loop start time
//                val start = System.currentTimeMillis()
//
//
//                // Calculate time it took to complete update
//                val finished = System.currentTimeMillis()
//                val timeTaken = finished - start
//
//                // Determine sleep time, check if below zero - continue if so
//                val sleepTime = cycleTime - timeTaken
//                if (sleepTime <= 0L) {
//                    continue
//                }
//                try { Thread.sleep(sleepTime) }
//                catch (e: Exception) { log.w("openPosUpdaterThread Sleep Interrupted") }
//                // Sleep so that it is exactly 5000 ms when next loop starts
//            }
//        }
//
//        openPosUpdaterThread!!.start()
//    }
//
//
//    private fun updateOptionPositionWithQuote(pos: OptionPosition, q: OptionQuote): OptionPosition? {
//        try {
//            val time = System.currentTimeMillis()
//            val curTotalValue = q.bidPrice * pos.quantity.toDouble() - pos.fees
//            val gainDollarTotal = curTotalValue - pos.totalPrice
//            val gainDollarPer = q.bidPrice - pos.pricePer
//            val gainPercent = (((q.bidPrice - .65) - pos.pricePer) / pos.pricePer) * 100
//
//            val highGdPerCon =
//                if (gainDollarPer > pos.highestGainDollarPerContract) gainDollarPer
//                else pos.highestGainDollarPerContract
//            val highGdTotal =
//                if (gainDollarTotal > pos.highestGainDollarTotal) gainDollarTotal
//                else pos.highestGainDollarTotal
//
//            val lowestGdPerCon =
//                if (gainDollarPer < pos.lowestGainDollarPerContract) gainDollarPer
//                else pos.lowestGainDollarPerContract
//            val lowestGdTotal =
//                if (gainDollarTotal < pos.lowestGainDollarPerTotal) gainDollarTotal
//                else pos.lowestGainDollarPerTotal
//
//            val highestGPct =
//                if (gainPercent > pos.highestGainPercent) gainPercent
//                else pos.highestGainPercent
//            val lowGPct =
//                if (gainPercent < pos.lowestGainPercent) gainPercent
//                else pos.lowestGainPercent
//
//            val updatedPosition = pos.copy(
//                lastUpdatedTimestampMs = time,
//                lastUpdatedDate = java.util.Date(time),
//                bid = q.bidPrice,
//                ask = q.askPrice,
//                mark = q.mark,
//                highPrice = q.highPrice,
//                lowPrice = q.lowPrice,
//                openPrice = q.openPrice,
//                totalVolume = q.totalVolume,
//                volatility = q.volatility,
//                delta = q.delta,
//                gamma = q.gamma,
//                theta = q.theta,
//                vega = q.vega,
//                rho = q.rho,
//                openInterest = q.openInterest,
//                timeValue = q.timeValue,
//                theoreticalOptionValue = q.theoreticalOptionValue,
//                dte = q.daysToExpiration,
//                intrinsicValue = q.moneyIntrinsicValue,
//                high52Week = q.weekHigh52,
//                low52Week = q.weekLow52,
//                inTheMoney = itmCheck(pos.putCall, pos.strikePrice, q.underlyingPrice),
//                itmDistance = itmDistanceCheck(pos.putCall, pos.strikePrice, q.underlyingPrice),
//
//                gainLossDollarTotal = gainDollarTotal,
//                gainLossDollarPer = gainDollarPer,
//                gainLossPercent = gainPercent,
//
//                underlyingPriceCurrent = q.underlyingPrice,
//
//                curValuePerContract = q.bidPrice - .65,
//                curValueOfPosition = (q.bidPrice * pos.quantity.toDouble()) - (1.3 * pos.quantity.toDouble()),
//
//                highestGainDollarPerContract = highGdPerCon,
//                highestGainDollarTotal = highGdTotal,
//                lowestGainDollarPerContract = lowestGdPerCon,
//                lowestGainDollarPerTotal = lowestGdTotal,
//                highestGainPercent = highestGPct,
//                lowestGainPercent = lowGPct
//            )
//            return updatedPosition
//        } catch (e: Exception) {
//            Log.i(
//                "PosManager.posUpdate()", "Failed to update position ID: ${pos.id} " +
//                        "SYM: ${pos.optionSymbol}",
//                e.stackTraceToString()
//            )
//            return null
//        }
//    }
//
//
//    private fun updateAllOpenPositionsWithQuoteData(): Boolean {
//
//        val openPosList = getAllOpenPositionsFromDB()
//        // Get quotes for updates, return un-updated list on fail
//        val syms = openPosList.map { it.optionSymbol }
//        if (syms.isEmpty()){ return true }      // If there are no positions
//        val quoteMap = Common.csApi.getMultiOptionQuote(syms) ?: return false
//
//        // Loop through positions and create updated list
//        val newList = mutableListOf<OptionPosition>()
//        for (p in openPosList) {
//
//            // Get quote for pos out of quote map, if null add un-updated pos to list to be returned
//            val q = quoteMap.get(p.optionSymbol)
//            if (q == null) {
//                newList.add(p)
//                Log.w(
//                    "PosManager.UpdateOpenPositions()",
//                    "Quote not found!. " +
//                            "Update Failed For: ${p.optionSymbol} ID: ${p.id}\\tLast Update: ${p.lastUpdatedDate}"
//                )
//                continue
//            }
//
//            // Update pos with quote data, if fail add un-updated pos to list to be returned
//            val updatedPos = updateOptionPositionWithQuote(p, q)
//            if (updatedPos == null) {
//                Log.i(
//                    "PosManager.UpdateOpenPositions()", "Update Failed For: ${p.optionSymbol}\t" +
//                            "ID: ${p.id}\tLast Update: ${p.lastUpdatedDate}"
//                )
//                newList.add(p)      // Add the un-updated position to new list
//                continue
//            }
//
//            // Add updated pos to list
//            newList.add(updatedPos)
//        }
//        val s = System.currentTimeMillis()
//        // Update database
//        updateMultipleOptionPositionEntriesToDB(newList)
//        return true
//    }
//
//
//    private fun itmCheck(putCall: String, strike: Double, curStockValue: Double): Boolean {
//        if (putCall == "CALL") {
//            return if (strike > curStockValue) false else true
//        } else {
//            return if (strike > curStockValue) true else false
//        }
//    }
//
//
//    private fun isPutOrCall(optionSymbol: String): String {
//        val putCall = optionSymbol[12]
//        return if (putCall == 'C') "CALL" else "PUT"
//    }
//
//
//    private fun itmDistanceCheck(putCall: String, strike: Double, curStockValue: Double): Double {
//        if (putCall == "CALL") {
//            // cp - st
//            return curStockValue - strike
//        } else {
//            // st - cp
//            return strike - curStockValue
//        }
//    }
//
//
//    /** Returns a list of all positions active today. Either open, or if they were closed today. */
//    fun getAllDaysActivePositionsForBot(botName: String): List<OptionPosition> {
//        val allPos = getBotsAllOptionPositionsFromDB(botName)
//
//        // Get the start of the current day (12:01 AM)
//        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
//            .toInstant()
//            .toEpochMilli()
//
//        val open = allPos.filter { it.closeTimestampMs == 0L }
//        val closed = allPos.filter { it.closeTimestampMs > startOfDay }
//
//        return open + closed
//    }
//
//
//    // Database ///////////////////////////////////////////////////////////////////////////////////////////////////////
//
//    private fun jsonToMap(json: String): Map<String, String> {
//        val gson = Common.gson
//        val mapType = object : TypeToken<Map<String, String>>() {}.type
//        return gson.fromJson(json, mapType)
//    }
//
//
//    private fun mapToJson(map: Map<String, String>): String {
//        return Common.gson.toJson(map)
//    }
//
//
//    private fun mapResultSetToOptionPosition(resultSet: ResultSet): OptionPosition {
//        return OptionPosition(
//            id = UUID.fromString(resultSet.getString("id")),
//            botName = resultSet.getString("bot_name"),
//            botDesc = resultSet.getString("bot_desc"),
//            stockSymbol = resultSet.getString("stock_symbol"),
//            optionSymbol = resultSet.getString("option_symbol"),
//            lastUpdatedTimestampMs = resultSet.getLong("last_updated_timestamp_ms"),
//            lastUpdatedDate = Date(resultSet.getLong("last_updated_date")),
//            openTimestampMs = resultSet.getLong("open_timestamp_ms"),
//            openDate = Date(resultSet.getLong("open_date")),
//            closeTimestampMs = resultSet.getLong("close_timestamp_ms"),
//            closeDate = Date(resultSet.getLong("close_date")),
//            isPaperTrade = resultSet.getInt("is_paper_trade") == 1,
//            putCall = resultSet.getString("put_call"),
//            strikePrice = resultSet.getDouble("strike_price"),
//            lastTradingDay = resultSet.getLong("last_trading_day"),
//            expirationDate = resultSet.getString("expiration_date"),
//            description = resultSet.getString("description"),
//            dteAtPurchaseTime = resultSet.getInt("dte_at_purchase_time"),
//            quantity = resultSet.getInt("quantity"),
//            fees = resultSet.getDouble("fees"),
//            pricePer = resultSet.getDouble("price_per"),
//            totalPrice = resultSet.getDouble("total_price"),
//            bid = resultSet.getDouble("bid"),
//            ask = resultSet.getDouble("ask"),
//            mark = resultSet.getDouble("mark"),
//            highPrice = resultSet.getDouble("high_price"),
//            lowPrice = resultSet.getDouble("low_price"),
//            openPrice = resultSet.getDouble("open_price"),
//            totalVolume = resultSet.getInt("total_volume"),
//            daysPercentChangeAtPurchaseTime = resultSet.getDouble("days_percent_change_at_purchase_time"),
//            daysNetChangeAtPurchaseTime = resultSet.getDouble("days_net_change_at_purchase_time"),
//            volatility = resultSet.getDouble("volatility"),
//            delta = resultSet.getDouble("delta"),
//            gamma = resultSet.getDouble("gamma"),
//            theta = resultSet.getDouble("theta"),
//            vega = resultSet.getDouble("vega"),
//            rho = resultSet.getDouble("rho"),
//            openInterest = resultSet.getInt("open_interest"),
//            timeValue = resultSet.getDouble("time_value"),
//            theoreticalOptionValue = resultSet.getDouble("theoretical_option_value"),
//            dte = resultSet.getInt("dte"),
//            intrinsicValue = resultSet.getDouble("intrinsic_value"),
//            high52Week = resultSet.getDouble("high_52_week"),
//            low52Week = resultSet.getDouble("low_52_week"),
//            inTheMoney = resultSet.getInt("in_the_money") == 1,
//            itmDistance = resultSet.getDouble("itm_distance"),
//            gainLossDollarTotal = resultSet.getDouble("gain_loss_dollar_total"),
//            gainLossDollarPer = resultSet.getDouble("gain_loss_dollar_per"),
//            gainLossPercent = resultSet.getDouble("gain_loss_percent"),
//            quoteAtOpenJson = resultSet.getString("quote_at_open_json"),
//            quoteAtCloseJson = resultSet.getString("quote_at_close_json"),
//            takeProfitDollarTarget = resultSet.getDouble("take_profit_dollar_target"),
//            takeProfitPercentTarget = resultSet.getDouble("take_profit_percent_target"),
//            stopLossDollarTarget = resultSet.getDouble("stop_loss_dollar_target"),
//            stopLossPercentTarget = resultSet.getDouble("stop_loss_percent_target"),
//            closeReason = resultSet.getString("close_reason"),
//            underlyingPriceCurrent = resultSet.getDouble("underlying_price_current"),
//            underlyingPriceAtPurchase = resultSet.getDouble("underlying_price_at_purchase"),
//            curValuePerContract = resultSet.getDouble("cur_value_per_contract"),
//            curValueOfPosition = resultSet.getDouble("cur_value_of_position"),
//            highestGainDollarPerContract = resultSet.getDouble("highest_gain_dollar_per_contract"),
//            highestGainDollarTotal = resultSet.getDouble("highest_gain_dollar_total"),
//            lowestGainDollarPerContract = resultSet.getDouble("lowest_gain_dollar_per_contract"),
//            lowestGainDollarPerTotal = resultSet.getDouble("lowest_gain_dollar_total"),
//            highestGainPercent = resultSet.getDouble("highest_gain_percent"),
//            lowestGainPercent = resultSet.getDouble("lowest_gain_percent"),
//            extraData = jsonToMap(resultSet.getString("extra_data"))
//        )
//    }
//
//
//    private fun buildHikariDatasource(): HikariDataSource {
//        val config = HikariConfig().apply {
//            jdbcUrl = PositionsManager.jdbcUrl
//            driverClassName = "org.sqlite.JDBC"
//            maximumPoolSize = 10
//            connectionInitSql = "PRAGMA journal_mode=WAL;"
//            addDataSourceProperty("cachePrepStmts", "true")
//            addDataSourceProperty("prepStmtCacheSize", "250")
//            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
//        }
//        return HikariDataSource(config)
//    }
//
//
//    private fun getConnection(): Connection {
//        return dbDatasource.getConnection()
//    }
//
//
//    private fun createOptionPositionTable() {
//
//        // SQL statement to create table
//        val createTableSQL = """
//                    CREATE TABLE IF NOT EXISTS $OPTION_POS_TABLE_NAME (
//                        id TEXT PRIMARY KEY,
//                        bot_name TEXT NOT NULL,
//                        bot_desc TEXT NOT NULL,
//                        stock_symbol TEXT NOT NULL,
//                        option_symbol TEXT NOT NULL,
//                        last_updated_timestamp_ms INTEGER NOT NULL,
//                        last_updated_date INTEGER NOT NULL,
//                        open_timestamp_ms INTEGER NOT NULL,
//                        open_date INTEGER NOT NULL,
//                        close_timestamp_ms INTEGER NOT NULL,
//                        close_date INTEGER NOT NULL,
//                        is_paper_trade INTEGER NOT NULL,
//                        put_call TEXT NOT NULL,
//                        strike_price REAL NOT NULL,
//                        last_trading_day INTEGER NOT NULL,
//                        expiration_date TEXT NOT NULL,
//                        description TEXT NOT NULL,
//                        dte_at_purchase_time INTEGER NOT NULL,
//                        quantity INTEGER NOT NULL,
//                        fees REAL NOT NULL,
//                        price_per REAL NOT NULL,
//                        total_price REAL NOT NULL,
//                        bid REAL NOT NULL,
//                        ask REAL NOT NULL,
//                        mark REAL NOT NULL,
//                        high_price REAL NOT NULL,
//                        low_price REAL NOT NULL,
//                        open_price REAL NOT NULL,
//                        total_volume INTEGER NOT NULL,
//                        days_percent_change_at_purchase_time REAL NOT NULL,
//                        days_net_change_at_purchase_time REAL NOT NULL,
//                        volatility REAL NOT NULL,
//                        delta REAL NOT NULL,
//                        gamma REAL NOT NULL,
//                        theta REAL NOT NULL,
//                        vega REAL NOT NULL,
//                        rho REAL NOT NULL,
//                        open_interest INTEGER NOT NULL,
//                        time_value REAL NOT NULL,
//                        theoretical_option_value REAL NOT NULL,
//                        dte INTEGER NOT NULL,
//                        intrinsic_value REAL NOT NULL,
//                        high_52_week REAL NOT NULL,
//                        low_52_week REAL NOT NULL,
//                        in_the_money INTEGER NOT NULL,
//                        itm_distance REAL NOT NULL,
//                        gain_loss_dollar_total REAL NOT NULL,
//                        gain_loss_dollar_per REAL NOT NULL,
//                        gain_loss_percent REAL NOT NULL,
//                        take_profit_dollar_target REAL NOT NULL,
//                        take_profit_percent_target REAL NOT NULL,
//                        stop_loss_dollar_target REAL NOT NULL,
//                        stop_loss_percent_target REAL NOT NULL,
//                        close_reason TEXT NOT NULL,
//                        quote_at_open_json TEXT NOT NULL,
//                        quote_at_close_json TEXT NOT NULL,
//                        underlying_price_current REAL NOT NULL,
//                        underlying_price_at_purchase REAL NOT NULL,
//                        cur_value_per_contract REAL NOT NULL,
//                        cur_value_of_position REAL NOT NULL,
//                        highest_gain_dollar_per_contract REAL NOT NULL,
//                        highest_gain_dollar_total REAL NOT NULL,
//                        lowest_gain_dollar_per_contract REAL NOT NULL,
//                        lowest_gain_dollar_total REAL NOT NULL,
//                        highest_gain_percent REAL NOT NULL,
//                        lowest_gain_percent REAL NOT NULL,
//                        extra_data TEXT NOT NULL
//                        );
//                    """.trimIndent()
//
//        // Establish connection to the database
//        getConnection().use { con ->
//            // Create statement and execute SQL
//            con.createStatement().use { statement ->
//                statement.execute(createTableSQL)
//            }
//        }
//    }
//
//
//    fun insertOptionPositionToDB(optionPosition: OptionPosition) {
//
//        synchronized(lockForWrites){
//            val insertSQL = """
//                INSERT OR REPLACE INTO $OPTION_POS_TABLE_NAME (
//                    id, bot_name, bot_desc, stock_symbol, option_symbol,
//                    last_updated_timestamp_ms, last_updated_date, open_timestamp_ms,
//                    open_date, close_timestamp_ms, close_date, is_paper_trade,
//                    put_call, strike_price, last_trading_day, expiration_date,
//                    description, dte_at_purchase_time, quantity, fees, price_per,
//                    total_price, bid, ask, mark, high_price, low_price,
//                    open_price, total_volume, days_percent_change_at_purchase_time,
//                    days_net_change_at_purchase_time, volatility, delta,
//                    gamma, theta, vega, rho, open_interest, time_value,
//                    theoretical_option_value, dte, intrinsic_value, high_52_week,
//                    low_52_week, in_the_money, itm_distance, gain_loss_dollar_total,
//                    gain_loss_dollar_per, gain_loss_percent,
//                    take_profit_dollar_target, take_profit_percent_target,
//                    stop_loss_dollar_target, stop_loss_percent_target, close_reason,
//                    quote_at_open_json, quote_at_close_json, underlying_price_current,
//                    underlying_price_at_purchase, cur_value_per_contract, cur_value_of_position,
//                    highest_gain_dollar_per_contract,
//                    highest_gain_dollar_total,
//                    lowest_gain_dollar_per_contract,
//                    lowest_gain_dollar_total,
//                    highest_gain_percent,
//                    lowest_gain_percent,
//                    extra_data
//
//                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
//                 ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
//                  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
//                """.trimIndent()
//
//            try {
//                getConnection().use { conn ->
//                    conn.prepareStatement(insertSQL).use { pstmt ->
//                        pstmt.setString(1, optionPosition.id.toString())
//                        pstmt.setString(2, optionPosition.botName)
//                        pstmt.setString(3, optionPosition.botDesc)
//                        pstmt.setString(4, optionPosition.stockSymbol)
//                        pstmt.setString(5, optionPosition.optionSymbol)
//                        pstmt.setLong(6, optionPosition.lastUpdatedTimestampMs)
//                        pstmt.setLong(7, optionPosition.lastUpdatedDate.time)
//                        pstmt.setLong(8, optionPosition.openTimestampMs)
//                        pstmt.setLong(9, optionPosition.openDate.time)
//                        pstmt.setLong(10, optionPosition.closeTimestampMs)
//                        pstmt.setLong(11, optionPosition.closeDate.time)
//                        pstmt.setInt(12, if (optionPosition.isPaperTrade) 1 else 0)
//                        pstmt.setString(13, optionPosition.putCall)
//                        pstmt.setDouble(14, optionPosition.strikePrice)
//                        pstmt.setLong(15, optionPosition.lastTradingDay)
//                        pstmt.setString(16, optionPosition.expirationDate)
//                        pstmt.setString(17, optionPosition.description)
//                        pstmt.setInt(18, optionPosition.dteAtPurchaseTime)
//                        pstmt.setInt(19, optionPosition.quantity)
//                        pstmt.setDouble(20, optionPosition.fees)
//                        pstmt.setDouble(21, optionPosition.pricePer)
//                        pstmt.setDouble(22, optionPosition.totalPrice)
//                        pstmt.setDouble(23, optionPosition.bid)
//                        pstmt.setDouble(24, optionPosition.ask)
//                        pstmt.setDouble(25, optionPosition.mark)
//                        pstmt.setDouble(26, optionPosition.highPrice)
//                        pstmt.setDouble(27, optionPosition.lowPrice)
//                        pstmt.setDouble(28, optionPosition.openPrice)
//                        pstmt.setInt(29, optionPosition.totalVolume)
//                        pstmt.setDouble(30, optionPosition.daysPercentChangeAtPurchaseTime)
//                        pstmt.setDouble(31, optionPosition.daysNetChangeAtPurchaseTime)
//                        pstmt.setDouble(32, optionPosition.volatility)
//                        pstmt.setDouble(33, optionPosition.delta)
//                        pstmt.setDouble(34, optionPosition.gamma)
//                        pstmt.setDouble(35, optionPosition.theta)
//                        pstmt.setDouble(36, optionPosition.vega)
//                        pstmt.setDouble(37, optionPosition.rho)
//                        pstmt.setInt(38, optionPosition.openInterest)
//                        pstmt.setDouble(39, optionPosition.timeValue)
//                        pstmt.setDouble(40, optionPosition.theoreticalOptionValue)
//                        pstmt.setInt(41, optionPosition.dte)
//                        pstmt.setDouble(42, optionPosition.intrinsicValue)
//                        pstmt.setDouble(43, optionPosition.high52Week)
//                        pstmt.setDouble(44, optionPosition.low52Week)
//                        pstmt.setInt(45, if (optionPosition.inTheMoney) 1 else 0)
//                        pstmt.setDouble(46, optionPosition.itmDistance)
//                        pstmt.setDouble(47, optionPosition.gainLossDollarTotal)
//                        pstmt.setDouble(48, optionPosition.gainLossDollarPer)
//                        pstmt.setDouble(49, optionPosition.gainLossPercent)
//                        pstmt.setDouble(50, optionPosition.takeProfitDollarTarget)
//                        pstmt.setDouble(51, optionPosition.takeProfitPercentTarget)
//                        pstmt.setDouble(52, optionPosition.stopLossDollarTarget)
//                        pstmt.setDouble(53, optionPosition.stopLossPercentTarget)
//                        pstmt.setString(54, optionPosition.closeReason)
//                        pstmt.setString(55, optionPosition.quoteAtOpenJson)
//                        pstmt.setString(56, optionPosition.quoteAtCloseJson)
//                        pstmt.setDouble(57, optionPosition.underlyingPriceCurrent)
//                        pstmt.setDouble(58, optionPosition.underlyingPriceAtPurchase)
//                        pstmt.setDouble(59, optionPosition.curValuePerContract)
//                        pstmt.setDouble(60, optionPosition.curValueOfPosition)
//                        pstmt.setDouble(61, optionPosition.highestGainDollarPerContract)
//                        pstmt.setDouble(62, optionPosition.highestGainDollarTotal)
//                        pstmt.setDouble(63, optionPosition.lowestGainDollarPerContract)
//                        pstmt.setDouble(64, optionPosition.lowestGainDollarPerTotal)
//                        pstmt.setDouble(65, optionPosition.highestGainPercent)
//                        pstmt.setDouble(66, optionPosition.lowestGainPercent)
//                        pstmt.setString(
//                            67, mapToJson(optionPosition.extraData)
//                        )
//                        pstmt.executeUpdate()
//                    }
//                }
//            } catch (e: SQLException) {
//                Log.w("insertOptionPosition()", "Failed to Insert Option Position", e.stackTraceToString())
//            }
//        }
//    }
//
//
//    fun insertMultipleOptionPositionsToDB(entries: List<OptionPosition>) {
//
//        synchronized(lockForWrites){
//            val sql = """
//                INSERT OR REPLACE INTO $OPTION_POS_TABLE_NAME (
//                    id, bot_name, bot_desc, stock_symbol, option_symbol, last_updated_timestamp_ms,
//                    last_updated_date, open_timestamp_ms, open_date, close_timestamp_ms, close_date,
//                    is_paper_trade, put_call, strike_price, last_trading_day, expiration_date, description,
//                    dte_at_purchase_time, quantity, fees, price_per, total_price, bid, ask, mark, high_price,
//                    low_price, open_price, total_volume, days_percent_change_at_purchase_time,
//                    days_net_change_at_purchase_time, volatility, delta, gamma, theta, vega, rho,
//                    open_interest, time_value, theoretical_option_value, dte, intrinsic_value, high_52_week,
//                    low_52_week, in_the_money, itm_distance, gain_loss_dollar_total, gain_loss_dollar_per,
//                    gain_loss_percent, take_profit_dollar_target, take_profit_percent_target, stop_loss_dollar_target,
//                    stop_loss_percent_target, close_reason, quote_at_open_json, quote_at_close_json,
//                    underlying_price_current, underlying_price_at_purchase, cur_value_per_contract,
//                    cur_value_of_position,
//                    highest_gain_dollar_per_contract,
//                    highest_gain_dollar_total,
//                    lowest_gain_dollar_per_contract,
//                    lowest_gain_dollar_total,
//                    highest_gain_percent,
//                    lowest_gain_percent,
//                    extra_data
//                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
//                 ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
//                  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
//            """.trimIndent()
//
//            val connection = getConnection()
//            connection.autoCommit = false
//
//            val statement: PreparedStatement = connection.prepareStatement(sql)
//            try {
//                for (entry in entries) {
//                    statement.setString(1, entry.id.toString())
//                    statement.setString(2, entry.botName)
//                    statement.setString(3, entry.botDesc)
//                    statement.setString(4, entry.stockSymbol)
//                    statement.setString(5, entry.optionSymbol)
//                    statement.setLong(6, entry.lastUpdatedTimestampMs)
//                    statement.setLong(7, entry.lastUpdatedDate.time)
//                    statement.setLong(8, entry.openTimestampMs)
//                    statement.setLong(9, entry.openDate.time)
//                    statement.setLong(10, entry.closeTimestampMs)
//                    statement.setLong(11, entry.closeDate.time)
//                    statement.setBoolean(12, entry.isPaperTrade)
//                    statement.setString(13, entry.putCall)
//                    statement.setDouble(14, entry.strikePrice)
//                    statement.setLong(15, entry.lastTradingDay)
//                    statement.setString(16, entry.expirationDate)
//                    statement.setString(17, entry.description)
//                    statement.setInt(18, entry.dteAtPurchaseTime)
//                    statement.setInt(19, entry.quantity)
//                    statement.setDouble(20, entry.fees)
//                    statement.setDouble(21, entry.pricePer)
//                    statement.setDouble(22, entry.totalPrice)
//                    statement.setDouble(23, entry.bid)
//                    statement.setDouble(24, entry.ask)
//                    statement.setDouble(25, entry.mark)
//                    statement.setDouble(26, entry.highPrice)
//                    statement.setDouble(27, entry.lowPrice)
//                    statement.setDouble(28, entry.openPrice)
//                    statement.setInt(29, entry.totalVolume)
//                    statement.setDouble(30, entry.daysPercentChangeAtPurchaseTime)
//                    statement.setDouble(31, entry.daysNetChangeAtPurchaseTime)
//                    statement.setDouble(32, entry.volatility)
//                    statement.setDouble(33, entry.delta)
//                    statement.setDouble(34, entry.gamma)
//                    statement.setDouble(35, entry.theta)
//                    statement.setDouble(36, entry.vega)
//                    statement.setDouble(37, entry.rho)
//                    statement.setInt(38, entry.openInterest)
//                    statement.setDouble(39, entry.timeValue)
//                    statement.setDouble(40, entry.theoreticalOptionValue)
//                    statement.setInt(41, entry.dte)
//                    statement.setDouble(42, entry.intrinsicValue)
//                    statement.setDouble(43, entry.high52Week)
//                    statement.setDouble(44, entry.low52Week)
//                    statement.setBoolean(45, entry.inTheMoney)
//                    statement.setDouble(46, entry.itmDistance)
//                    statement.setDouble(47, entry.gainLossDollarTotal)
//                    statement.setDouble(48, entry.gainLossDollarPer)
//                    statement.setDouble(49, entry.gainLossPercent)
//                    statement.setDouble(50, entry.takeProfitDollarTarget)
//                    statement.setDouble(51, entry.takeProfitPercentTarget)
//                    statement.setDouble(52, entry.stopLossDollarTarget)
//                    statement.setDouble(53, entry.stopLossPercentTarget)
//                    statement.setString(54, entry.closeReason)
//                    statement.setString(55, entry.quoteAtOpenJson)
//                    statement.setString(56, entry.quoteAtCloseJson)
//                    statement.setDouble(57, entry.underlyingPriceCurrent)
//                    statement.setDouble(58, entry.underlyingPriceAtPurchase)
//                    statement.setDouble(59, entry.curValuePerContract)
//                    statement.setDouble(60, entry.curValueOfPosition)
//                    statement.setDouble(61, entry.highestGainDollarPerContract)
//                    statement.setDouble(62, entry.highestGainDollarTotal)
//                    statement.setDouble(63, entry.lowestGainDollarPerContract)
//                    statement.setDouble(64, entry.lowestGainDollarPerTotal)
//                    statement.setDouble(65, entry.highestGainPercent)
//                    statement.setDouble(66, entry.lowestGainPercent)
//                    statement.setString(
//                        67, mapToJson(entry.extraData)
//                    )
//
//                    statement.addBatch()
//                }
//                statement.executeBatch()
//                connection.commit()
//            } catch (e: Exception) {
//                connection.rollback()
//                Log.w(
//                    "PositionsDb.insertMultipleOptionPositions()",
//                    "Failed to insert. Exception triggered",
//                    e.stackTraceToString()
//                )
//            } finally {
//                statement.close()
//                connection.autoCommit = true
//            }
//        }
//
//    }
//
//
//    fun updateOptionPositionEntryToDB(optionPosition: OptionPosition) {
//        synchronized(lockForWrites){
//            val updateSQL = """
//                UPDATE $OPTION_POS_TABLE_NAME SET
//                    bot_name = ?,
//                    bot_desc = ?,
//                    stock_symbol = ?,
//                    option_symbol = ?,
//                    last_updated_timestamp_ms = ?,
//                    last_updated_date = ?,
//                    open_timestamp_ms = ?,
//                    open_date = ?,
//                    close_timestamp_ms = ?,
//                    close_date = ?,
//                    is_paper_trade = ?,
//                    put_call = ?,
//                    strike_price = ?,
//                    last_trading_day = ?,
//                    expiration_date = ?,
//                    description = ?,
//                    dte_at_purchase_time = ?,
//                    quantity = ?,
//                    fees = ?,
//                    price_per = ?,
//                    total_price = ?,
//                    bid = ?,
//                    ask = ?,
//                    mark = ?,
//                    high_price = ?,
//                    low_price = ?,
//                    open_price = ?,
//                    total_volume = ?,
//                    days_percent_change_at_purchase_time = ?,
//                    days_net_change_at_purchase_time = ?,
//                    volatility = ?,
//                    delta = ?,
//                    gamma = ?,
//                    theta = ?,
//                    vega = ?,
//                    rho = ?,
//                    open_interest = ?,
//                    time_value = ?,
//                    theoretical_option_value = ?,
//                    dte = ?,
//                    intrinsic_value = ?,
//                    high_52_week = ?,
//                    low_52_week = ?,
//                    in_the_money = ?,
//                    itm_distance = ?,
//                    gain_loss_dollar_total = ?,
//                    gain_loss_dollar_per = ?,
//                    gain_loss_percent = ?,
//                    take_profit_dollar_target = ?,
//                    take_profit_percent_target = ?,
//                    stop_loss_dollar_target = ?,
//                    stop_loss_percent_target = ?,
//                    close_reason = ?,
//                    quote_at_open_json = ?,
//                    quote_at_close_json = ?,
//                    underlying_price_current = ?,
//                    underlying_price_at_purchase = ?,
//                    cur_value_per_contract = ?,
//                    cur_value_of_position = ?,
//                    highest_gain_dollar_per_contract = ?,
//                    highest_gain_dollar_total = ?,
//                    lowest_gain_dollar_per_contract = ?,
//                    lowest_gain_dollar_total = ?,
//                    highest_gain_percent = ?,
//                    lowest_gain_percent = ?,
//                    extra_data = ?
//
//                    WHERE id = ?
//                """.trimIndent()
//
//
//            try {
//                getConnection().use { conn ->
//                    conn.prepareStatement(updateSQL).use { pstmt ->
//                        pstmt.setString(1, optionPosition.botName)
//                        pstmt.setString(2, optionPosition.botDesc)
//                        pstmt.setString(3, optionPosition.stockSymbol)
//                        pstmt.setString(4, optionPosition.optionSymbol)
//                        pstmt.setLong(5, optionPosition.lastUpdatedTimestampMs)
//                        pstmt.setLong(6, optionPosition.lastUpdatedDate.time)
//                        pstmt.setLong(7, optionPosition.openTimestampMs)
//                        pstmt.setLong(8, optionPosition.openDate.time)
//                        pstmt.setLong(9, optionPosition.closeTimestampMs)
//                        pstmt.setLong(10, optionPosition.closeDate.time)
//                        pstmt.setInt(11, if (optionPosition.isPaperTrade) 1 else 0)
//                        pstmt.setString(12, optionPosition.putCall)
//                        pstmt.setDouble(13, optionPosition.strikePrice)
//                        pstmt.setLong(14, optionPosition.lastTradingDay)
//                        pstmt.setString(15, optionPosition.expirationDate)
//                        pstmt.setString(16, optionPosition.description)
//                        pstmt.setInt(17, optionPosition.dteAtPurchaseTime)
//                        pstmt.setInt(18, optionPosition.quantity)
//                        pstmt.setDouble(19, optionPosition.fees)
//                        pstmt.setDouble(20, optionPosition.pricePer)
//                        pstmt.setDouble(21, optionPosition.totalPrice)
//                        pstmt.setDouble(22, optionPosition.bid)
//                        pstmt.setDouble(23, optionPosition.ask)
//                        pstmt.setDouble(24, optionPosition.mark)
//                        pstmt.setDouble(25, optionPosition.highPrice)
//                        pstmt.setDouble(26, optionPosition.lowPrice)
//                        pstmt.setDouble(27, optionPosition.openPrice)
//                        pstmt.setInt(28, optionPosition.totalVolume)
//                        pstmt.setDouble(29, optionPosition.daysPercentChangeAtPurchaseTime)
//                        pstmt.setDouble(30, optionPosition.daysNetChangeAtPurchaseTime)
//                        pstmt.setDouble(31, optionPosition.volatility)
//                        pstmt.setDouble(32, optionPosition.delta)
//                        pstmt.setDouble(33, optionPosition.gamma)
//                        pstmt.setDouble(34, optionPosition.theta)
//                        pstmt.setDouble(35, optionPosition.vega)
//                        pstmt.setDouble(36, optionPosition.rho)
//                        pstmt.setInt(37, optionPosition.openInterest)
//                        pstmt.setDouble(38, optionPosition.timeValue)
//                        pstmt.setDouble(39, optionPosition.theoreticalOptionValue)
//                        pstmt.setInt(40, optionPosition.dte)
//                        pstmt.setDouble(41, optionPosition.intrinsicValue)
//                        pstmt.setDouble(42, optionPosition.high52Week)
//                        pstmt.setDouble(43, optionPosition.low52Week)
//                        pstmt.setInt(44, if (optionPosition.inTheMoney) 1 else 0)
//                        pstmt.setDouble(45, optionPosition.itmDistance)
//                        pstmt.setDouble(46, optionPosition.gainLossDollarTotal)
//                        pstmt.setDouble(47, optionPosition.gainLossDollarPer)
//                        pstmt.setDouble(48, optionPosition.gainLossPercent)
//                        pstmt.setDouble(49, optionPosition.takeProfitDollarTarget)
//                        pstmt.setDouble(50, optionPosition.takeProfitPercentTarget)
//                        pstmt.setDouble(51, optionPosition.stopLossDollarTarget)
//                        pstmt.setDouble(52, optionPosition.stopLossPercentTarget)
//                        pstmt.setString(53, optionPosition.closeReason)
//                        pstmt.setString(54, optionPosition.quoteAtOpenJson)
//                        pstmt.setString(55, optionPosition.quoteAtCloseJson)
//                        pstmt.setDouble(56, optionPosition.underlyingPriceCurrent)
//                        pstmt.setDouble(57, optionPosition.underlyingPriceAtPurchase)
//                        pstmt.setDouble(58, optionPosition.curValuePerContract)
//                        pstmt.setDouble(59, optionPosition.curValueOfPosition)
//                        pstmt.setDouble(60, optionPosition.highestGainDollarPerContract)
//                        pstmt.setDouble(61, optionPosition.highestGainDollarTotal)
//                        pstmt.setDouble(62, optionPosition.lowestGainDollarPerContract)
//                        pstmt.setDouble(63, optionPosition.lowestGainDollarPerTotal)
//                        pstmt.setDouble(64, optionPosition.highestGainPercent)
//                        pstmt.setDouble(65, optionPosition.lowestGainPercent)
//                        pstmt.setString(66, mapToJson(optionPosition.extraData))
//                        pstmt.setString(67, optionPosition.id.toString())
//
//                        pstmt.executeUpdate()
//                    }
//                }
//            } catch (e: SQLException) {
//                Log.w(
//                    "updateOptionPosition()", "Failed to update Pos ID: ${optionPosition.id.toString()}",
//                    e.stackTraceToString()
//                )
//            }
//        }
//    }
//
//
//    /** Updates list of optionPositions in Database.
//     *
//     * Note: Time taken for 150 positions: 20ms*/
//    fun updateMultipleOptionPositionEntriesToDB(optionPositions: List<OptionPosition>) {
//        synchronized(lockForWrites){
//            try {
//                val connection = getConnection()
//                connection.autoCommit = false
//
//                val sql = """
//                    UPDATE $OPTION_POS_TABLE_NAME SET
//                        bot_name = ?,
//                        bot_desc = ?,
//                        stock_symbol = ?,
//                        option_symbol = ?,
//                        last_updated_timestamp_ms = ?,
//                        last_updated_date = ?,
//                        open_timestamp_ms = ?,
//                        open_date = ?,
//                        close_timestamp_ms = ?,
//                        close_date = ?,
//                        is_paper_trade = ?,
//                        put_call = ?,
//                        strike_price = ?,
//                        last_trading_day = ?,
//                        expiration_date = ?,
//                        description = ?,
//                        dte_at_purchase_time = ?,
//                        quantity = ?,
//                        fees = ?,
//                        price_per = ?,
//                        total_price = ?,
//                        bid = ?,
//                        ask = ?,
//                        mark = ?,
//                        high_price = ?,
//                        low_price = ?,
//                        open_price = ?,
//                        total_volume = ?,
//                        days_percent_change_at_purchase_time = ?,
//                        days_net_change_at_purchase_time = ?,
//                        volatility = ?,
//                        delta = ?,
//                        gamma = ?,
//                        theta = ?,
//                        vega = ?,
//                        rho = ?,
//                        open_interest = ?,
//                        time_value = ?,
//                        theoretical_option_value = ?,
//                        dte = ?,
//                        intrinsic_value = ?,
//                        high_52_week = ?,
//                        low_52_week = ?,
//                        in_the_money = ?,
//                        itm_distance = ?,
//                        gain_loss_dollar_total = ?,
//                        gain_loss_dollar_per = ?,
//                        gain_loss_percent = ?,
//                        take_profit_dollar_target = ?,
//                        take_profit_percent_target = ?,
//                        stop_loss_dollar_target = ?,
//                        stop_loss_percent_target = ?,
//                        close_reason = ?,
//                        quote_at_open_json = ?,
//                        quote_at_close_json = ?,
//                        underlying_price_current = ?,
//                        underlying_price_at_purchase = ?,
//                        cur_value_per_contract = ?,
//                        cur_value_of_position = ?,
//                        highest_gain_dollar_per_contract = ?,
//                        highest_gain_dollar_total = ?,
//                        lowest_gain_dollar_per_contract = ?,
//                        lowest_gain_dollar_total = ?,
//                        highest_gain_percent = ?,
//                        lowest_gain_percent = ?,
//                        extra_data = ?
//                    WHERE id = ?
//        """.trimIndent()
//
//                connection.use { conn ->
//                    conn.prepareStatement(sql).use { psmt ->
//                        for (position in optionPositions) {
//                            psmt.setString(1, position.botName)
//                            psmt.setString(2, position.botDesc)
//                            psmt.setString(3, position.stockSymbol)
//                            psmt.setString(4, position.optionSymbol)
//                            psmt.setLong(5, position.lastUpdatedTimestampMs)
//                            psmt.setLong(6, position.lastUpdatedDate.time)
//                            psmt.setLong(7, position.openTimestampMs)
//                            psmt.setLong(8, position.openDate.time)
//                            psmt.setLong(9, position.closeTimestampMs)
//                            psmt.setLong(10, position.closeDate.time)
//                            psmt.setBoolean(11, position.isPaperTrade)
//                            psmt.setString(12, position.putCall)
//                            psmt.setDouble(13, position.strikePrice)
//                            psmt.setLong(14, position.lastTradingDay)
//                            psmt.setString(15, position.expirationDate)
//                            psmt.setString(16, position.description)
//                            psmt.setInt(17, position.dteAtPurchaseTime)
//                            psmt.setInt(18, position.quantity)
//                            psmt.setDouble(19, position.fees)
//                            psmt.setDouble(20, position.pricePer)
//                            psmt.setDouble(21, position.totalPrice)
//                            psmt.setDouble(22, position.bid)
//                            psmt.setDouble(23, position.ask)
//                            psmt.setDouble(24, position.mark)
//                            psmt.setDouble(25, position.highPrice)
//                            psmt.setDouble(26, position.lowPrice)
//                            psmt.setDouble(27, position.openPrice)
//                            psmt.setInt(28, position.totalVolume)
//                            psmt.setDouble(29, position.daysPercentChangeAtPurchaseTime)
//                            psmt.setDouble(30, position.daysNetChangeAtPurchaseTime)
//                            psmt.setDouble(31, position.volatility)
//                            psmt.setDouble(32, position.delta)
//                            psmt.setDouble(33, position.gamma)
//                            psmt.setDouble(34, position.theta)
//                            psmt.setDouble(35, position.vega)
//                            psmt.setDouble(36, position.rho)
//                            psmt.setInt(37, position.openInterest)
//                            psmt.setDouble(38, position.timeValue)
//                            psmt.setDouble(39, position.theoreticalOptionValue)
//                            psmt.setInt(40, position.dte)
//                            psmt.setDouble(41, position.intrinsicValue)
//                            psmt.setDouble(42, position.high52Week)
//                            psmt.setDouble(43, position.low52Week)
//                            psmt.setBoolean(44, position.inTheMoney)
//                            psmt.setDouble(45, position.itmDistance)
//                            psmt.setDouble(46, position.gainLossDollarTotal)
//                            psmt.setDouble(47, position.gainLossDollarPer)
//                            psmt.setDouble(48, position.gainLossPercent)
//                            psmt.setDouble(49, position.takeProfitDollarTarget)
//                            psmt.setDouble(50, position.takeProfitPercentTarget)
//                            psmt.setDouble(51, position.stopLossDollarTarget)
//                            psmt.setDouble(52, position.stopLossPercentTarget)
//                            psmt.setString(53, position.closeReason)
//                            psmt.setString(54, position.quoteAtOpenJson)
//                            psmt.setString(55, position.quoteAtCloseJson)
//                            psmt.setDouble(56, position.underlyingPriceCurrent)
//                            psmt.setDouble(57, position.underlyingPriceAtPurchase)
//                            psmt.setDouble(58, position.curValuePerContract)
//                            psmt.setDouble(59, position.curValueOfPosition)
//                            psmt.setDouble(60, position.highestGainDollarPerContract)
//                            psmt.setDouble(61, position.highestGainDollarTotal)
//                            psmt.setDouble(62, position.lowestGainDollarPerContract)
//                            psmt.setDouble(63, position.lowestGainDollarPerTotal)
//                            psmt.setDouble(64, position.highestGainPercent)
//                            psmt.setDouble(65, position.lowestGainPercent)
//                            psmt.setString(66, mapToJson(position.extraData))
//                            psmt.setString(67, position.id.toString())
//                            psmt.addBatch()
//                        }
//                        // TODO -- I think
//                        psmt.executeBatch()
//                        conn.commit()
//                    }
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//
//    fun deleteOptionPositionFromDB(id: UUID) {
//        synchronized(lockForWrites){
//            val deleteSQL = "DELETE FROM $OPTION_POS_TABLE_NAME WHERE id = ?"
//
//            try {
//                getConnection().use { connection ->
//                    connection.prepareStatement(deleteSQL).use { preparedStatement ->
//                        preparedStatement.setString(1, id.toString())
//                        preparedStatement.executeUpdate()
//                    }
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//
//    fun getBotsAllOptionPositionsFromDB(botName: String): List<OptionPosition> {
//        val searchSQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE bot_name = ?"
//
//        val r = mutableListOf<OptionPosition>().apply {
//            getConnection().use { connection ->
//                connection.prepareStatement(searchSQL).use { preparedStatement ->
//                    preparedStatement.setString(1, botName)
//                    preparedStatement.executeQuery().use { resultSet ->
//                        while (resultSet.next()) {
//                            add(mapResultSetToOptionPosition(resultSet))
//                        }
//                    }
//                }
//            }
//        }
//        return r
//    }
//
//
//    fun getBotsOpenOptionPositionsFromDB(botName: String): List<OptionPosition> {
//        val searchSQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE " +
//                "bot_name = ? " +
//                "AND close_timestamp_ms = ?"
//
//        val r = mutableListOf<OptionPosition>().apply {
//            getConnection().use { connection ->
//                connection.prepareStatement(searchSQL).use { preparedStatement ->
//                    preparedStatement.setString(1, botName)
//                    preparedStatement.setLong(2, 0)
//                    preparedStatement.executeQuery().use { resultSet ->
//                        while (resultSet.next()) {
//                            add(mapResultSetToOptionPosition(resultSet))
//                        }
//                    }
//                }
//            }
//        }
//        return r
//    }
//
//
//    fun getBotsAllDaysClosedOptionPositionsFromDB(botName: String): List<OptionPosition> {
//
//        // Get the start of the current day (12:01 AM)
//        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
//            .toInstant()
//            .toEpochMilli()
//
//        val searchSQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE " +
//                "bot_name = ? " +
//                "AND close_timestamp_ms > $startOfDay;"
//
//        val r = mutableListOf<OptionPosition>().apply {
//            getConnection().use { connection ->
//                connection.prepareStatement(searchSQL).use { preparedStatement ->
//                    preparedStatement.setString(1, botName)
//                    preparedStatement.executeQuery().use { resultSet ->
//                        while (resultSet.next()) {
//                            add(mapResultSetToOptionPosition(resultSet))
//                        }
//                    }
//                }
//            }
//        }
//        return r
//    }
//
//
//    fun getBotsAllPositionsOpenedTodayFromDB(botName: String): List<OptionPosition> {
//        // Get the start of the current day (12:01 AM)
//        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
//            .toInstant()
//            .toEpochMilli()
//
//        // SQL to get all positions opened today after 12:01 AM
//        val searchSQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE " +
//                "bot_name = ? " +
//                "AND open_timestamp_ms > $startOfDay;"
//
//        val r = mutableListOf<OptionPosition>().apply {
//            getConnection().use { connection ->
//                connection.prepareStatement(searchSQL).use { preparedStatement ->
//                    preparedStatement.setString(1, botName)
//                    preparedStatement.executeQuery().use { resultSet ->
//                        while (resultSet.next()) {
//                            add(mapResultSetToOptionPosition(resultSet))
//                        }
//                    }
//                }
//            }
//        }
//        return r
//    }
//
//
//    fun getOptionPositionByIdFromDB(id: UUID): OptionPosition? {
//        return try {
//            val searchSQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE id = ?"
//
//            val r = mutableListOf<OptionPosition>().apply {
//                getConnection().use { connection ->
//                    connection.prepareStatement(searchSQL).use { preparedStatement ->
//                        preparedStatement.setString(1, id.toString())
//
//                        preparedStatement.executeQuery().use { resultSet ->
//                            while (resultSet.next()) {
//                                add(mapResultSetToOptionPosition(resultSet))
//                            }
//                        }
//                    }
//                }
//            }
//            if (r.isNotEmpty()) {
//                r.first()
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//
//    fun getAllOpenPositionsFromDB(): List<OptionPosition> {
//
//        // SQL to get all positions open
//        val searchSQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE " +
//                "close_timestamp_ms = 0;"
//
//        val r = mutableListOf<OptionPosition>().apply {
//            getConnection().use { connection ->
//                connection.prepareStatement(searchSQL).use { preparedStatement ->
//                    preparedStatement.executeQuery().use { resultSet ->
//                        while (resultSet.next()) {
//                            add(mapResultSetToOptionPosition(resultSet))
//                        }
//                    }
//                }
//            }
//        }
//        return r
//    }
//}
