package com.github.wigggy.botsbase.systems.bot_tools

import java.time.*
import java.time.temporal.ChronoUnit

object MarketTimeUtil {


    fun isMarketOpen(): Boolean {
        // Get the current time in the EST time zone
        val zoneId = ZoneId.of("America/New_York")
        val now = ZonedDateTime.now(zoneId)

        // Check if it is a week day (Monday to Friday)
        val dayOfWeek = now.dayOfWeek
        if (dayOfWeek.value in 1..5) {
            // Check if the current time is between 9:30 AM and 4:00 PM EST
            val currentTime = now.toLocalTime()
            val marketOpenTime = LocalTime.of(9, 30)
            val marketCloseTime = LocalTime.of(16, 0)

            return currentTime.isAfter(marketOpenTime) && currentTime.isBefore(marketCloseTime)
        }

        return false
    }


    fun getNextMarketOpenTimeInMillis(): Long {
        val zoneId = ZoneId.of("America/New_York")
        val now = ZonedDateTime.now(zoneId)

        if (isMarketOpen()) {
            return now.toInstant().toEpochMilli()
        }

        // Calculate the next market opening time
        var nextMarketOpen = now.toLocalDate().atTime(9, 30).atZone(zoneId)

        // If today is a weekday but the time is after 9:30 AM, or it's a weekend, move to the next weekday
        if (now.toLocalTime().isAfter(LocalTime.of(9, 30)) || now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY) {
            nextMarketOpen = nextMarketOpen.plusDays(1)
            while (nextMarketOpen.dayOfWeek == DayOfWeek.SATURDAY || nextMarketOpen.dayOfWeek == DayOfWeek.SUNDAY) {
                nextMarketOpen = nextMarketOpen.plusDays(1)
            }
        }

        return nextMarketOpen.toInstant().toEpochMilli()
    }


    fun getMarketWaitTimeInMillis(): Long {
        val zoneId = ZoneId.of("America/New_York")
        val now = ZonedDateTime.now(zoneId)

        if (isMarketOpen()) {
            return System.currentTimeMillis()
        }

        // Calculate the next market opening time
        var nextMarketOpen = now.toLocalDate().atTime(9, 30).atZone(zoneId)

        // If today is a weekday but the time is after 9:30 AM, or it's a weekend, move to the next weekday
        if (now.toLocalTime().isAfter(LocalTime.of(9, 30)) || now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY) {
            nextMarketOpen = nextMarketOpen.plusDays(1)
            while (nextMarketOpen.dayOfWeek == DayOfWeek.SATURDAY || nextMarketOpen.dayOfWeek == DayOfWeek.SUNDAY) {
                nextMarketOpen = nextMarketOpen.plusDays(1)
            }
        }

        return ChronoUnit.MILLIS.between(now, nextMarketOpen)
    }


    fun executeTaskWhenMarketOpens(task: () -> Unit) {
        val nextMarketOpenTime = getNextMarketOpenTimeInMillis()
        val waitTime = nextMarketOpenTime - System.currentTimeMillis()

        if (waitTime < 0){
            task()
            return
        }
        try {
            Thread.sleep(waitTime)
        }catch (e: Exception){
            // Return withoug task() execution
            return
        }
        task()
    }


    /** Returns true/false if time is after given hour and minute.
     *
     * Note hour is in 24h format. so 1pm is 13, and 4pm is 16*/
    fun isTimeAfter(hour: Int, minute: Int): Boolean {
        // Get the current time
        val currentTime = LocalTime.now()

        // Create a LocalTime object for the given hour and minute
        val givenTime = LocalTime.of(hour, minute)

        // Compare the current time with the given time
        return currentTime.isAfter(givenTime)
    }


    fun isTimeBefore(hour: Int, minute: Int): Boolean{
        // Get the current time
        val currentTime = LocalTime.now()

        // Create a LocalTime object for the given hour and minute
        val givenTime = LocalTime.of(hour, minute)

        // Compare the current time with the given time
        return currentTime.isBefore(givenTime)
    }


    fun hasNewDayStartedSinceTs(ts: Long): Boolean {
        // Convert the timestamp to LocalDate
        val timestampDate = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()

        // Get the current date
        val currentDate = LocalDate.now()

        // Check if the timestamp date is before the current date
        return timestampDate.isBefore(currentDate)
    }
}


fun main() {
    val yd = System.currentTimeMillis() - (16_400_000)
    println(MarketTimeUtil.hasNewDayStartedSinceTs(yd))
}