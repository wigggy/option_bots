package com.github.wigggy.botsbase.systems.data

import com.github.wigggy.botsbase.systems.bot_tools.ColorLogger
import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date
import java.util.UUID
import com.google.gson.Gson
import java.time.LocalDate
import java.time.ZoneId

class OptionPositionDb(private val botName: String) {

    private val log = ColorLogger("OptionPositionDb-$botName")
    private val jdbcUrl = "jdbc:sqlite:_savedata/option_position_db_for_$botName.db"
    private val OPTION_POS_TABLE_NAME = "option_positions"
    private val dbDataSource: HikariDataSource = buildHikariDatasource()
    private val gson = Gson()

    private fun buildHikariDatasource(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = this@OptionPositionDb.jdbcUrl
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
        return this.dbDataSource.connection
    }

    init {
        createTable()
    }

    private fun createTable() {
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS $OPTION_POS_TABLE_NAME (
            
                id TEXT PRIMARY KEY,
                botName TEXT,
                botDesc TEXT,
                stockSymbol TEXT,
                optionSymbol TEXT,
                
                lastUpdatedTimestampMs INTEGER,
                lastUpdatedDate INTEGER,
                openTimestampMs INTEGER,
                openDate INTEGER,
                closeTimestampMs INTEGER,
                
                closeDate INTEGER,
                isPaperTrade INTEGER,
                putCall TEXT,
                strikePrice REAL,
                lastTradingDay INTEGER,
                
                expirationDate TEXT,
                description TEXT,
                dteAtPurchaseTime INTEGER,
                quantity INTEGER,
                fees REAL,
                
                pricePer REAL,
                totalPrice REAL,
                bid REAL,
                ask REAL,
                mark REAL,
                
                highPrice REAL,
                lowPrice REAL,
                openPrice REAL,
                totalVolume INTEGER,
                daysPercentChangeAtPurchaseTime REAL,
                
                daysNetChangeAtPurchaseTime REAL,
                volatility REAL,
                delta REAL,
                gamma REAL,
                theta REAL,
                
                vega REAL,
                rho REAL,
                openInterest INTEGER,
                timeValue REAL,
                theoreticalOptionValue REAL,
                
                dte INTEGER,
                intrinsicValue REAL,
                high52Week REAL,
                low52Week REAL,
                inTheMoney INTEGER,
                
                itmDistance REAL,
                gainLossDollarTotal REAL,
                gainLossDollarPer REAL,
                gainLossPercent REAL,
                takeProfitDollarTarget REAL,
                
                takeProfitPercentTarget REAL,
                stopLossDollarTarget REAL,
                stopLossPercentTarget REAL,
                closeReason TEXT,
                quoteAtOpenJson TEXT,
                
                quoteAtCloseJson TEXT,
                underlyingPriceCurrent REAL,
                underlyingPriceAtPurchase REAL,
                curValuePerContract REAL,
                curValueOfPosition REAL,
                
                highestGainDollarPerContract REAL,
                highestGainDollarTotal REAL,
                lowestGainDollarPerContract REAL,
                lowestGainDollarPerTotal REAL,
                highestGainPercent REAL,
                
                lowestGainPercent REAL,
                extraData TEXT
            )
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(createTableSQL)
            }
        }
    }

    fun insertOptionPosition(op: OptionPosition): Boolean {
        val insertSQL = """
            INSERT INTO $OPTION_POS_TABLE_NAME VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        getConnection().use { conn ->
            conn.prepareStatement(insertSQL).use { stmt ->
                stmt.setString(1, op.id.toString())
                stmt.setString(2, op.botName)
                stmt.setString(3, op.botDesc)
                stmt.setString(4, op.stockSymbol)
                stmt.setString(5, op.optionSymbol)
                stmt.setLong(6, op.lastUpdatedTimestampMs)
                stmt.setTimestamp(7, Timestamp(op.lastUpdatedDate.time))
                stmt.setLong(8, op.openTimestampMs)
                stmt.setTimestamp(9, Timestamp(op.openDate.time))
                stmt.setLong(10, op.closeTimestampMs)
                stmt.setTimestamp(11, Timestamp(op.closeDate.time))
                stmt.setInt(12, if (op.isPaperTrade) 1 else 0)
                stmt.setString(13, op.putCall)
                stmt.setDouble(14, op.strikePrice)
                stmt.setLong(15, op.lastTradingDay)
                stmt.setString(16, op.expirationDate)
                stmt.setString(17, op.description)
                stmt.setInt(18, op.dteAtPurchaseTime)
                stmt.setInt(19, op.quantity)
                stmt.setDouble(20, op.fees)
                stmt.setDouble(21, op.pricePer)
                stmt.setDouble(22, op.totalPrice)
                stmt.setDouble(23, op.bid)
                stmt.setDouble(24, op.ask)
                stmt.setDouble(25, op.mark)
                stmt.setDouble(26, op.highPrice)
                stmt.setDouble(27, op.lowPrice)
                stmt.setDouble(28, op.openPrice)
                stmt.setInt(29, op.totalVolume)
                stmt.setDouble(30, op.daysPercentChangeAtPurchaseTime)
                stmt.setDouble(31, op.daysNetChangeAtPurchaseTime)
                stmt.setDouble(32, op.volatility)
                stmt.setDouble(33, op.delta)
                stmt.setDouble(34, op.gamma)
                stmt.setDouble(35, op.theta)
                stmt.setDouble(36, op.vega)
                stmt.setDouble(37, op.rho)
                stmt.setInt(38, op.openInterest)
                stmt.setDouble(39, op.timeValue)
                stmt.setDouble(40, op.theoreticalOptionValue)
                stmt.setInt(41, op.dte)
                stmt.setDouble(42, op.intrinsicValue)
                stmt.setDouble(43, op.high52Week)
                stmt.setDouble(44, op.low52Week)
                stmt.setInt(45, if (op.inTheMoney) 1 else 0)
                stmt.setDouble(46, op.itmDistance)
                stmt.setDouble(47, op.gainLossDollarTotal)
                stmt.setDouble(48, op.gainLossDollarPer)
                stmt.setDouble(49, op.gainLossPercent)
                stmt.setDouble(50, op.takeProfitDollarTarget)
                stmt.setDouble(51, op.takeProfitPercentTarget)
                stmt.setDouble(52, op.stopLossDollarTarget)
                stmt.setDouble(53, op.stopLossPercentTarget)
                stmt.setString(54, op.closeReason)
                stmt.setString(55, op.quoteAtOpenJson)
                stmt.setString(56, op.quoteAtCloseJson)
                stmt.setDouble(57, op.underlyingPriceCurrent)
                stmt.setDouble(58, op.underlyingPriceAtPurchase)
                stmt.setDouble(59, op.curValuePerContract)
                stmt.setDouble(60, op.curValueOfPosition)
                stmt.setDouble(61, op.highestGainDollarPerContract)
                stmt.setDouble(62, op.highestGainDollarTotal)
                stmt.setDouble(63, op.lowestGainDollarPerContract)
                stmt.setDouble(64, op.lowestGainDollarPerTotal)
                stmt.setDouble(65, op.highestGainPercent)
                stmt.setDouble(66, op.lowestGainPercent)
                stmt.setString(67, gson.toJson(op.extraData))
                return stmt.executeUpdate() > 0
            }
        }
    }

    fun insertMultipleOptionPositions(opList: List<OptionPosition>): Boolean {
        return opList.all { insertOptionPosition(it) }
    }

    fun getOptionPositionByID(id: UUID): OptionPosition? {
        val querySQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(querySQL).use { stmt ->
                stmt.setString(1, id.toString())
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) mapResultSetToOptionPosition(rs) else null
                }
            }
        }
    }

    fun getAllOptionPositions(): List<OptionPosition> {
        val querySQL = "SELECT * FROM $OPTION_POS_TABLE_NAME"
        val result = mutableListOf<OptionPosition>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(querySQL).use { rs ->
                    while (rs.next()) {
                        result.add(mapResultSetToOptionPosition(rs))
                    }
                }
            }
        }
        return result
    }

    fun getAllOpenOptionPositions(): List<OptionPosition> {
        val querySQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE closeTimestampMs = 0"
        val result = mutableListOf<OptionPosition>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(querySQL).use { rs ->
                    while (rs.next()) {
                        result.add(mapResultSetToOptionPosition(rs))
                    }
                }
            }
        }
        return result
    }

    /** Returns a list of all positions active today. Either open, or if they were closed today. */
    fun getAllPositionsOfDay(): List<OptionPosition> {
        val allPos = getAllOptionPositions()

        // Get the start of the current day (12:01 AM)
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val open = allPos.filter { it.closeTimestampMs == 0L }
        val closed = allPos.filter { it.closeTimestampMs > startOfDay }

        return open + closed
    }

    fun getAllClosedOptionPositions(): List<OptionPosition> {
        val querySQL = "SELECT * FROM $OPTION_POS_TABLE_NAME WHERE closeTimestampMs != 0"
        val result = mutableListOf<OptionPosition>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(querySQL).use { rs ->
                    while (rs.next()) {
                        result.add(mapResultSetToOptionPosition(rs))
                    }
                }
            }
        }
        return result
    }

    fun updateOptionPosition(op: OptionPosition): Boolean {
        val updateSQL = """
            UPDATE $OPTION_POS_TABLE_NAME SET
                botName = ?, botDesc = ?, stockSymbol = ?, optionSymbol = ?, lastUpdatedTimestampMs = ?, 
                lastUpdatedDate = ?, openTimestampMs = ?, openDate = ?, closeTimestampMs = ?, closeDate = ?, 
                isPaperTrade = ?, putCall = ?, strikePrice = ?, lastTradingDay = ?, expirationDate = ?, 
                description = ?, dteAtPurchaseTime = ?, quantity = ?, fees = ?, pricePer = ?, totalPrice = ?, 
                bid = ?, ask = ?, mark = ?, highPrice = ?, lowPrice = ?, openPrice = ?, totalVolume = ?, 
                daysPercentChangeAtPurchaseTime = ?, daysNetChangeAtPurchaseTime = ?, volatility = ?, 
                delta = ?, gamma = ?, theta = ?, vega = ?, rho = ?, openInterest = ?, timeValue = ?, 
                theoreticalOptionValue = ?, dte = ?, intrinsicValue = ?, high52Week = ?, low52Week = ?, 
                inTheMoney = ?, itmDistance = ?, gainLossDollarTotal = ?, gainLossDollarPer = ?, gainLossPercent = ?, 
                takeProfitDollarTarget = ?, takeProfitPercentTarget = ?, stopLossDollarTarget = ?, 
                stopLossPercentTarget = ?, closeReason = ?, quoteAtOpenJson = ?, quoteAtCloseJson = ?, 
                underlyingPriceCurrent = ?, underlyingPriceAtPurchase = ?, curValuePerContract = ?, 
                curValueOfPosition = ?, highestGainDollarPerContract = ?, highestGainDollarTotal = ?, 
                lowestGainDollarPerContract = ?, lowestGainDollarPerTotal = ?, highestGainPercent = ?, 
                lowestGainPercent = ?, extraData = ? WHERE id = ?
        """.trimIndent()

        getConnection().use { conn ->
            conn.prepareStatement(updateSQL).use { stmt ->
                stmt.setString(1, op.botName)
                stmt.setString(2, op.botDesc)
                stmt.setString(3, op.stockSymbol)
                stmt.setString(4, op.optionSymbol)
                stmt.setLong(5, op.lastUpdatedTimestampMs)
                stmt.setTimestamp(6, Timestamp(op.lastUpdatedDate.time))
                stmt.setLong(7, op.openTimestampMs)
                stmt.setTimestamp(8, Timestamp(op.openDate.time))
                stmt.setLong(9, op.closeTimestampMs)
                stmt.setTimestamp(10, Timestamp(op.closeDate.time))
                stmt.setInt(11, if (op.isPaperTrade) 1 else 0)
                stmt.setString(12, op.putCall)
                stmt.setDouble(13, op.strikePrice)
                stmt.setLong(14, op.lastTradingDay)
                stmt.setString(15, op.expirationDate)
                stmt.setString(16, op.description)
                stmt.setInt(17, op.dteAtPurchaseTime)
                stmt.setInt(18, op.quantity)
                stmt.setDouble(19, op.fees)
                stmt.setDouble(20, op.pricePer)
                stmt.setDouble(21, op.totalPrice)
                stmt.setDouble(22, op.bid)
                stmt.setDouble(23, op.ask)
                stmt.setDouble(24, op.mark)
                stmt.setDouble(25, op.highPrice)
                stmt.setDouble(26, op.lowPrice)
                stmt.setDouble(27, op.openPrice)
                stmt.setInt(28, op.totalVolume)
                stmt.setDouble(29, op.daysPercentChangeAtPurchaseTime)
                stmt.setDouble(30, op.daysNetChangeAtPurchaseTime)
                stmt.setDouble(31, op.volatility)
                stmt.setDouble(32, op.delta)
                stmt.setDouble(33, op.gamma)
                stmt.setDouble(34, op.theta)
                stmt.setDouble(35, op.vega)
                stmt.setDouble(36, op.rho)
                stmt.setInt(37, op.openInterest)
                stmt.setDouble(38, op.timeValue)
                stmt.setDouble(39, op.theoreticalOptionValue)
                stmt.setInt(40, op.dte)
                stmt.setDouble(41, op.intrinsicValue)
                stmt.setDouble(42, op.high52Week)
                stmt.setDouble(43, op.low52Week)
                stmt.setInt(44, if (op.inTheMoney) 1 else 0)
                stmt.setDouble(45, op.itmDistance)
                stmt.setDouble(46, op.gainLossDollarTotal)
                stmt.setDouble(47, op.gainLossDollarPer)
                stmt.setDouble(48, op.gainLossPercent)
                stmt.setDouble(49, op.takeProfitDollarTarget)
                stmt.setDouble(50, op.takeProfitPercentTarget)
                stmt.setDouble(51, op.stopLossDollarTarget)
                stmt.setDouble(52, op.stopLossPercentTarget)
                stmt.setString(53, op.closeReason)
                stmt.setString(54, op.quoteAtOpenJson)
                stmt.setString(55, op.quoteAtCloseJson)
                stmt.setDouble(56, op.underlyingPriceCurrent)
                stmt.setDouble(57, op.underlyingPriceAtPurchase)
                stmt.setDouble(58, op.curValuePerContract)
                stmt.setDouble(59, op.curValueOfPosition)
                stmt.setDouble(60, op.highestGainDollarPerContract)
                stmt.setDouble(61, op.highestGainDollarTotal)
                stmt.setDouble(62, op.lowestGainDollarPerContract)
                stmt.setDouble(63, op.lowestGainDollarPerTotal)
                stmt.setDouble(64, op.highestGainPercent)
                stmt.setDouble(65, op.lowestGainPercent)
                stmt.setString(66, gson.toJson(op.extraData))
                stmt.setString(67, op.id.toString())
                return stmt.executeUpdate() > 0
            }
        }
    }

    fun updateMultipleOptionPositions(opList: List<OptionPosition>): Boolean {
        return opList.all { updateOptionPosition(it) }
    }

    fun deleteOptionPosition(op: OptionPosition): Boolean {
        val deleteSQL = "DELETE FROM $OPTION_POS_TABLE_NAME WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(deleteSQL).use { stmt ->
                stmt.setString(1, op.id.toString())
                return stmt.executeUpdate() > 0
            }
        }
    }

    private fun mapResultSetToOptionPosition(rs: ResultSet): OptionPosition {
        return OptionPosition(
            UUID.fromString(rs.getString("id")),
            rs.getString("botName"),
            rs.getString("botDesc"),
            rs.getString("stockSymbol"),
            rs.getString("optionSymbol"),
            rs.getLong("lastUpdatedTimestampMs"),
            Date(rs.getLong("lastUpdatedDate")),
            rs.getLong("openTimestampMs"),
            Date(rs.getLong("openDate")),
            rs.getLong("closeTimestampMs"),
            Date(rs.getLong("closeDate")),
            rs.getInt("isPaperTrade") == 1,
            rs.getString("putCall"),
            rs.getDouble("strikePrice"),
            rs.getLong("lastTradingDay"),
            rs.getString("expirationDate"),
            rs.getString("description"),
            rs.getInt("dteAtPurchaseTime"),
            rs.getInt("quantity"),
            rs.getDouble("fees"),
            rs.getDouble("pricePer"),
            rs.getDouble("totalPrice"),
            rs.getDouble("bid"),
            rs.getDouble("ask"),
            rs.getDouble("mark"),
            rs.getDouble("highPrice"),
            rs.getDouble("lowPrice"),
            rs.getDouble("openPrice"),
            rs.getInt("totalVolume"),
            rs.getDouble("daysPercentChangeAtPurchaseTime"),
            rs.getDouble("daysNetChangeAtPurchaseTime"),
            rs.getDouble("volatility"),
            rs.getDouble("delta"),
            rs.getDouble("gamma"),
            rs.getDouble("theta"),
            rs.getDouble("vega"),
            rs.getDouble("rho"),
            rs.getInt("openInterest"),
            rs.getDouble("timeValue"),
            rs.getDouble("theoreticalOptionValue"),
            rs.getInt("dte"),
            rs.getDouble("intrinsicValue"),
            rs.getDouble("high52Week"),
            rs.getDouble("low52Week"),
            rs.getInt("inTheMoney") == 1,
            rs.getDouble("itmDistance"),
            rs.getDouble("gainLossDollarTotal"),
            rs.getDouble("gainLossDollarPer"),
            rs.getDouble("gainLossPercent"),
            rs.getDouble("takeProfitDollarTarget"),
            rs.getDouble("takeProfitPercentTarget"),
            rs.getDouble("stopLossDollarTarget"),
            rs.getDouble("stopLossPercentTarget"),
            rs.getString("closeReason"),
            rs.getString("quoteAtOpenJson"),
            rs.getString("quoteAtCloseJson"),
            rs.getDouble("underlyingPriceCurrent"),
            rs.getDouble("underlyingPriceAtPurchase"),
            rs.getDouble("curValuePerContract"),
            rs.getDouble("curValueOfPosition"),
            rs.getDouble("highestGainDollarPerContract"),
            rs.getDouble("highestGainDollarTotal"),
            rs.getDouble("lowestGainDollarPerContract"),
            rs.getDouble("lowestGainDollarPerTotal"),
            rs.getDouble("highestGainPercent"),
            rs.getDouble("lowestGainPercent"),
            gson.fromJson(rs.getString("extraData"), Map::class.java) as Map<String, String>
        )
    }
}