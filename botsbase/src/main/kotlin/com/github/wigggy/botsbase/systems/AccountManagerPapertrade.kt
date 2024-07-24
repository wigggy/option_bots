package com.github.wigggy.botsbase.systems

import OptionPositionDb
import com.github.wigggy.botsbase.systems.interfaces.AccountManager
import java.time.LocalDate
import java.time.ZoneId

class AccountManagerPapertrade(
    private val db: OptionPositionDb,
    private val startingPapertradeBalance: Double
): AccountManager {

    /* TODO Use pos manager, get 'all pos list' calculate balances */

    override fun getBuyingPower(): Double {

        val pList = db.getAllOptionPositions()

        var bal = startingPapertradeBalance

        // Get Cash bal
        for (p in pList){
            if (p.closeTimestampMs != 0L){
                bal -= p.totalPrice
                bal += p.curValueOfPosition
            }
            else {
                bal -= p.totalPrice
            }
        }

        // Calculate how much of teh bal has been used today from pos closed today
        // Get the start of the current day (12:01 AM)
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val closedTodayList = pList.filter { it.closeTimestampMs != 0L && it.openTimestampMs >= startOfDay }
        for (p in closedTodayList){
            bal -= p.totalPrice
        }

        return bal
    }

    override fun getTotalBalance(): Double {
        val pList = db.getAllOptionPositions()

        var bal = startingPapertradeBalance

        for (p in pList){

            // Closed Pos
            if (p.closeTimestampMs != 0L){
                bal -= p.totalPrice
                bal += p.curValueOfPosition
            }
            // Open Pos
            else {
                bal -= p.totalPrice
                bal += p.curValueOfPosition
            }
        }

        return bal
    }

    override fun getCashBalance(): Double {
        val pList = db.getAllOptionPositions()

        var bal = startingPapertradeBalance
        for (p in pList){
            if (p.closeTimestampMs != 0L){
                bal -= p.totalPrice
                bal += p.curValueOfPosition
            }
            else {
                bal -= p.totalPrice
            }
        }
        return bal
    }
}