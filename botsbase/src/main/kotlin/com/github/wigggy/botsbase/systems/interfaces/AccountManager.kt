package com.github.wigggy.botsbase.systems.interfaces

interface AccountManager {

    /** Returns the total bal. Includes cash, and open positions */
    fun getTotalBalance(): Double

    /** Returns the cash balance. Open position value is not included */
    fun getCashBalance(): Double

    /** Returns the buying power  */
    fun getBuyingPower(): Double
}