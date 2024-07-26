package com.github.wigggy.botsbase.systems.data


import com.github.wigggy.botsbase.systems.bot_tools.Common
import com.github.wigggy.botsbase.systems.data.data_objs.BotState
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import com.github.wigggy.botsbase.systems.data.data_objs.TradePermissionCheckResults
import com.google.gson.reflect.TypeToken
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet

class BotStateDb(
    private val botName: String
) {

    private val TAG = BotStateDb::class.java.simpleName.toString()
    private val table_name = "BotStats"
    private val lockAccess = Any()
    private val hikariDataSource = buildHikariDatasource()

    init {
        createTable()
    }


    /** Creates new BotState and saves to database for future. */
    fun initNewBotState(
        botName: String,
        botDesc: String,
        dateBotCreatedMMDDYYYY: String,
        originalPapertradeBalance: Double,
        postCycleSleepTimeMs: Long
    ): BotState {
        val t = System.currentTimeMillis()
        val newBs = BotState(
            botName = botName,
            botDesc = botDesc,
            dateBotCreatedMMDDYYYY = dateBotCreatedMMDDYYYY,
            originalPapertradeBalance = originalPapertradeBalance,
            daysStartingBalance = originalPapertradeBalance,
            curTotalBalance = originalPapertradeBalance,
            daysStartingBalanceLastUpdate = t,
            postCycleSleepTimeMs = postCycleSleepTimeMs,
            lastUpdateTimestampMs = t,
            lastUpdateDate = Date(t),
            )

        insertBotState(newBs)
        return newBs
    }


    private fun getSavePath(): String {
        val spltName = botName.split(" ").joinToString("")
        val bn = "bot_state_db_for_$spltName"
        val savepath = "jdbc:sqlite:_savedata\\${bn}.db"
        return savepath
    }

    private fun buildHikariDatasource(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = getSavePath()
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 2
            connectionInitSql = "PRAGMA journal_mode=WAL;"
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }
        return HikariDataSource(config)
    }

    private fun getConnection(): Connection {
        return hikariDataSource.connection
    }


//    private fun getConnection(): Connection {
//        // Establish a connection to the database
//        return DriverManager.getConnection(getSavePath())
//    }


    private fun createTable() {

        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS $table_name (
                botName TEXT PRIMARY KEY,
                botDesc TEXT,
                dateBotCreatedMMDDYYYY TEXT,
                gainLossDollarDaysRealized REAL,
                gainLossPercentDaysRealized REAL,
                gainLossDollarDaysTotal REAL,
                gainLossDollarUnrealized REAL,
                gainLossDollarDaysRealizedAvg REAL,
                gainLossPercentDaysRealizedAvg REAL,
                nOpenPositions INTEGER,
                nClosedPositionsToday INTEGER,
                nTotalPositionsToday INTEGER,
                curCashPapertradeBalance REAL,
                originalPapertradeBalance REAL,
                buyingPower REAL,
                daysStartingBalance REAL,
                curOpenPositions TEXT,
                daysClosedPositions TEXT,
                tickerBlackList TEXT,
                lastUpdateTimestampMs INTEGER,
                lastUpdateDate TEXT,
                watchlist TEXT,
                postCycleSleepTimeMs INTEGER,
                power INTEGER,
                daysStartingBalanceLastUpdate INTEGER,
                curTotalBalance REAL,
                tradePermCheck TEXT,
                avgWinAmount REAL,
                avgLossAmount REAL,
                avgPercentGain REAL,
                avgPercentLoss REAL,
                daysWins INTEGER,
                daysLosses INTEGER,
                daysWinLossRatio REAL,
                daysWinPercentage REAL,
                daysBiggestGain REAL,
                daysBiggestLoss REAL,
                nPositionsOpenedToday INTEGER,
                daysTradedTicks TEXT,
                topGainingTicker TEXT,
                topGainingTickerGainDollar REAL,
                daysTickerGainMap TEXT,
                cycleCount INTEGER,
                blackListLastUpdatedMs INTEGER,
                watchlistLastUpdateMs INTEGER
            );
        """.trimIndent()

        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(createTableSQL)
            }
        }
    }


    fun insertBotState(botState: BotState) {
        synchronized(lockAccess) {
            val insertSQL = """
            INSERT OR REPLACE INTO $table_name (
                botName, botDesc, dateBotCreatedMMDDYYYY, gainLossDollarDaysRealized,
                 gainLossPercentDaysRealized, gainLossDollarDaysTotal, gainLossDollarUnrealized,
                gainLossDollarDaysRealizedAvg, gainLossPercentDaysRealizedAvg, nOpenPositions,
                 nClosedPositionsToday, nTotalPositionsToday, curCashPapertradeBalance,
                originalPapertradeBalance, buyingPower, daysStartingBalance,
                 curOpenPositions, daysClosedPositions, tickerBlackList, lastUpdateTimestampMs, lastUpdateDate,
                watchlist, postCycleSleepTimeMs, power, daysStartingBalanceLastUpdate,
                 curTotalBalance, tradePermCheck, avgWinAmount, avgLossAmount, avgPercentGain, 
                avgPercentLoss, daysWins, daysLosses, daysWinLossRatio, daysWinPercentage,
                 daysBiggestGain, daysBiggestLoss, nPositionsOpenedToday, daysTradedTicks, 
                topGainingTicker, topGainingTickerGainDollar, daysTickerGainMap,
                cycleCount, blackListLastUpdatedMs, watchlistLastUpdateMs
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
             ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """.trimIndent()

            val gson = Common.gson

            getConnection().use { connection ->
                connection.prepareStatement(insertSQL).use { preparedStatement ->
                    preparedStatement.setString(1, botState.botName)
                    preparedStatement.setString(2, botState.botDesc)
                    preparedStatement.setString(3, botState.dateBotCreatedMMDDYYYY)
                    preparedStatement.setDouble(4, botState.gainLossDollarDaysRealized)
                    preparedStatement.setDouble(5, botState.gainLossPercentDaysRealized)
                    preparedStatement.setDouble(6, botState.gainLossDollarDaysTotal)
                    preparedStatement.setDouble(7, botState.gainLossDollarUnrealized)
                    preparedStatement.setDouble(8, botState.gainLossDollarDaysRealizedAvg)
                    preparedStatement.setDouble(9, botState.gainLossPercentDaysRealizedAvg)
                    preparedStatement.setInt(10, botState.nOpenPositions)
                    preparedStatement.setInt(11, botState.nClosedPositionsToday)
                    preparedStatement.setInt(12, botState.nTotalPositionsToday)
                    preparedStatement.setDouble(13, botState.curCashPapertradeBalance)
                    preparedStatement.setDouble(14, botState.originalPapertradeBalance)
                    preparedStatement.setDouble(15, botState.buyingPower)
                    preparedStatement.setDouble(16, botState.daysStartingBalance)
                    preparedStatement.setString(17, gson.toJson(botState.curOpenPositions))
                    preparedStatement.setString(18, gson.toJson(botState.daysClosedPositions))
                    preparedStatement.setString(19, gson.toJson(botState.tickerBlackList))
                    preparedStatement.setLong(20, botState.lastUpdateTimestampMs)
                    preparedStatement.setString(21, gson.toJson(botState.lastUpdateDate))
                    preparedStatement.setString(22, gson.toJson(botState.watchlist))
                    preparedStatement.setLong(23, botState.postCycleSleepTimeMs)
                    preparedStatement.setInt(24, if (botState.power) 1 else 0)
                    preparedStatement.setLong(25, botState.daysStartingBalanceLastUpdate)
                    preparedStatement.setDouble(26, botState.curTotalBalance)
                    preparedStatement.setString(27, gson.toJson(botState.tradePermCheck))
                    preparedStatement.setDouble(28, botState.avgWinAmount)
                    preparedStatement.setDouble(29, botState.avgLossAmount)
                    preparedStatement.setDouble(30, botState.avgPercentGain)
                    preparedStatement.setDouble(31, botState.avgPercentLoss)
                    preparedStatement.setInt(32, botState.daysWins)
                    preparedStatement.setInt(33, botState.daysLosses)
                    preparedStatement.setDouble(34, botState.daysWinLossRatio)
                    preparedStatement.setDouble(35, botState.daysWinPercentage)
                    preparedStatement.setDouble(36, botState.daysBiggestGain)
                    preparedStatement.setDouble(37, botState.daysBiggestLoss)
                    preparedStatement.setInt(38, botState.nPositionsOpenedToday)
                    preparedStatement.setString(39, gson.toJson(botState.daysTradedTicks))
                    preparedStatement.setString(40, botState.topGainingTicker)
                    preparedStatement.setDouble(41, botState.topGainingTickerGainDollar)
                    preparedStatement.setString(42, gson.toJson(botState.daysTickerGainMap))
                    preparedStatement.setInt(43, botState.cycleCount)
                    preparedStatement.setLong(44, botState.blackListLastUpdatedMs)
                    preparedStatement.setLong(45, botState.watchlistLastUpdateMs)
                    preparedStatement.executeUpdate()
                }
            }
        }
    }


    fun getBotState(): BotState? {
        synchronized(lockAccess){
            val selectSQL = "SELECT * FROM $table_name WHERE botName = ?"
            getConnection().use { connection ->
                connection.prepareStatement(selectSQL).use { preparedStatement ->
                    preparedStatement.setString(1, botName)
                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        return mapResultSetToBotState(resultSet)
                    }
                }
            }
            return null
        }
    }


    fun updateBotState(botState: BotState){
        synchronized(lockAccess) {
            val updateSQL = """
            UPDATE $table_name SET 
                botDesc = ?, dateBotCreatedMMDDYYYY = ?, gainLossDollarDaysRealized = ?,
                 gainLossPercentDaysRealized = ?, gainLossDollarDaysTotal = ?, gainLossDollarUnrealized = ?,
                gainLossDollarDaysRealizedAvg = ?, gainLossPercentDaysRealizedAvg = ?,
                 nOpenPositions = ?, nClosedPositionsToday = ?,
                  nTotalPositionsToday = ?, curCashPapertradeBalance = ?,
                originalPapertradeBalance = ?, buyingPower = ?, daysStartingBalance = ?,
                 curOpenPositions = ?, daysClosedPositions = ?, tickerBlackList = ?,
                  lastUpdateTimestampMs = ?, lastUpdateDate = ?,
                watchlist = ?, postCycleSleepTimeMs = ?, power = ?,
                 daysStartingBalanceLastUpdate = ?, curTotalBalance = ?, tradePermCheck = ?,
                  avgWinAmount = ?, avgLossAmount = ?, avgPercentGain = ?, 
                avgPercentLoss = ?, daysWins = ?, daysLosses = ?,
                 daysWinLossRatio = ?, daysWinPercentage = ?, daysBiggestGain = ?,
                  daysBiggestLoss = ?, nPositionsOpenedToday = ?, daysTradedTicks = ?, 
                topGainingTicker = ?, topGainingTickerGainDollar = ?, daysTickerGainMap = ?,
                cycleCount = ?, blackListLastUpdatedMs = ?, watchlistLastUpdateMs = ?
                                
                
            WHERE botName = ?
        """.trimIndent()

            val gson = Common.gson

            getConnection().use { connection ->
                connection.prepareStatement(updateSQL).use { preparedStatement ->
                    preparedStatement.setString(1, botState.botDesc)
                    preparedStatement.setString(2, botState.dateBotCreatedMMDDYYYY)
                    preparedStatement.setDouble(3, botState.gainLossDollarDaysRealized)
                    preparedStatement.setDouble(4, botState.gainLossPercentDaysRealized)
                    preparedStatement.setDouble(5, botState.gainLossDollarDaysTotal)
                    preparedStatement.setDouble(6, botState.gainLossDollarUnrealized)
                    preparedStatement.setDouble(7, botState.gainLossDollarDaysRealizedAvg)
                    preparedStatement.setDouble(8, botState.gainLossPercentDaysRealizedAvg)
                    preparedStatement.setInt(9, botState.nOpenPositions)
                    preparedStatement.setInt(10, botState.nClosedPositionsToday)
                    preparedStatement.setInt(11, botState.nTotalPositionsToday)
                    preparedStatement.setDouble(12, botState.curCashPapertradeBalance)
                    preparedStatement.setDouble(13, botState.originalPapertradeBalance)
                    preparedStatement.setDouble(14, botState.buyingPower)
                    preparedStatement.setDouble(15, botState.daysStartingBalance)
                    preparedStatement.setString(16, gson.toJson(botState.curOpenPositions))
                    preparedStatement.setString(17, gson.toJson(botState.daysClosedPositions))
                    preparedStatement.setString(18, gson.toJson(botState.tickerBlackList))
                    preparedStatement.setLong(19, botState.lastUpdateTimestampMs)
                    preparedStatement.setString(20, gson.toJson(botState.lastUpdateDate))
                    preparedStatement.setString(21, gson.toJson(botState.watchlist))
                    preparedStatement.setLong(22, botState.postCycleSleepTimeMs)
                    preparedStatement.setInt(23, if (botState.power) 1 else 0)
                    preparedStatement.setLong(24, botState.daysStartingBalanceLastUpdate)
                    preparedStatement.setDouble(25, botState.curTotalBalance)
                    preparedStatement.setString(26, gson.toJson(botState.tradePermCheck))
                    preparedStatement.setDouble(27, botState.avgWinAmount)
                    preparedStatement.setDouble(28, botState.avgLossAmount)
                    preparedStatement.setDouble(29, botState.avgPercentGain)
                    preparedStatement.setDouble(30, botState.avgPercentLoss)
                    preparedStatement.setInt(31, botState.daysWins)
                    preparedStatement.setInt(32, botState.daysLosses)
                    preparedStatement.setDouble(33, botState.daysWinLossRatio)
                    preparedStatement.setDouble(34, botState.daysWinPercentage)
                    preparedStatement.setDouble(35, botState.daysBiggestGain)
                    preparedStatement.setDouble(36, botState.daysBiggestLoss)
                    preparedStatement.setInt(37, botState.nPositionsOpenedToday)
                    preparedStatement.setString(38, gson.toJson(botState.daysTradedTicks))
                    preparedStatement.setString(39, botState.topGainingTicker)
                    preparedStatement.setDouble(40, botState.topGainingTickerGainDollar)
                    preparedStatement.setString(41, gson.toJson(botState.daysTickerGainMap))
                    preparedStatement.setInt(42, botState.cycleCount)
                    preparedStatement.setLong(43, botState.blackListLastUpdatedMs)
                    preparedStatement.setLong(44, botState.watchlistLastUpdateMs)
                    preparedStatement.setString(45, botState.botName)

                    preparedStatement.executeUpdate()
                }
            }
        }
    }


    fun deleteBotState(){
        synchronized(lockAccess) {
            val deleteSQL = "DELETE FROM $table_name WHERE botName = ?"

            getConnection().use { connection ->
                connection.prepareStatement(deleteSQL).use { preparedStatement ->
                    preparedStatement.setString(1, botName)
                    preparedStatement.executeUpdate()
                }
            }
        }
    }


    private fun mapResultSetToBotState(resultSet: ResultSet): BotState {
        val gson = Common.gson
        return BotState(
            botName = resultSet.getString("botName"),
            botDesc = resultSet.getString("botDesc"),
            dateBotCreatedMMDDYYYY = resultSet.getString("dateBotCreatedMMDDYYYY"),
            gainLossDollarDaysRealized = resultSet.getDouble("gainLossDollarDaysRealized"),
            gainLossPercentDaysRealized = resultSet.getDouble("gainLossPercentDaysRealized"),
            gainLossDollarDaysTotal = resultSet.getDouble("gainLossDollarDaysTotal"),
            gainLossDollarUnrealized = resultSet.getDouble("gainLossDollarUnrealized"),
            gainLossDollarDaysRealizedAvg = resultSet.getDouble("gainLossDollarDaysRealizedAvg"),
            gainLossPercentDaysRealizedAvg = resultSet.getDouble("gainLossPercentDaysRealizedAvg"),
            nOpenPositions = resultSet.getInt("nOpenPositions"),
            nClosedPositionsToday = resultSet.getInt("nClosedPositionsToday"),
            nTotalPositionsToday = resultSet.getInt("nTotalPositionsToday"),
            curCashPapertradeBalance = resultSet.getDouble("curCashPapertradeBalance"),
            originalPapertradeBalance = resultSet.getDouble("originalPapertradeBalance"),
            buyingPower = resultSet.getDouble("buyingPower"),
            daysStartingBalance = resultSet.getDouble("daysStartingBalance"),
            curOpenPositions = gson.fromJson(resultSet.getString("curOpenPositions"),
                object : TypeToken<List<OptionPosition>>() {}.type),
            daysClosedPositions = gson.fromJson(resultSet.getString("daysClosedPositions"),
                object : TypeToken<List<OptionPosition>>() {}.type),
            tickerBlackList = gson.fromJson(resultSet.getString("tickerBlackList"),
                object : TypeToken<List<String>>() {}.type),
            lastUpdateTimestampMs = resultSet.getLong("lastUpdateTimestampMs"),
            lastUpdateDate = gson.fromJson(resultSet.getString("lastUpdateDate"), Date::class.java),
            watchlist = gson.fromJson(resultSet.getString("watchlist"),
                object : TypeToken<List<String>>() {}.type),
            postCycleSleepTimeMs = resultSet.getLong("postCycleSleepTimeMs"),
            power = resultSet.getInt("power") == 1,
            daysStartingBalanceLastUpdate = resultSet.getLong("daysStartingBalanceLastUpdate"),
            curTotalBalance = resultSet.getDouble("curTotalBalance"),
            tradePermCheck = gson.fromJson(resultSet.getString("tradePermCheck"),
                TradePermissionCheckResults::class.java),
            avgWinAmount = resultSet.getDouble("avgWinAmount"),
            avgLossAmount = resultSet.getDouble("avgLossAmount"),
            avgPercentGain = resultSet.getDouble("avgPercentGain"),
            avgPercentLoss = resultSet.getDouble("avgPercentLoss"),
            daysWins = resultSet.getInt("daysWins"),
            daysLosses = resultSet.getInt("daysLosses"),
            daysWinLossRatio = resultSet.getDouble("daysWinLossRatio"),
            daysWinPercentage = resultSet.getDouble("daysWinPercentage"),
            daysBiggestGain = resultSet.getDouble("daysBiggestGain"),
            daysBiggestLoss = resultSet.getDouble("daysBiggestLoss"),
            nPositionsOpenedToday = resultSet.getInt("nPositionsOpenedToday"),
            daysTradedTicks = gson.fromJson(resultSet.getString("daysTradedTicks"),
                object : TypeToken<List<String>>() {}.type),
            topGainingTicker = resultSet.getString("topGainingTicker"),
            topGainingTickerGainDollar = resultSet.getDouble("topGainingTickerGainDollar"),
            daysTickerGainMap = gson.fromJson( resultSet.getString("daysTickerGainMap"),
                object : TypeToken<Map<String, Double>>() {}.type),
            cycleCount = resultSet.getInt("cycleCount"),
            blackListLastUpdatedMs = resultSet.getLong("blackListLastUpdatedMs"),
            watchlistLastUpdateMs = resultSet.getLong("watchlistLastUpdateMs")
        )
    }

}