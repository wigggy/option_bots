package com.github.wigggy.botsbase.tests

import com.github.wigggy.botsbase.systems.bot_tools.Common

class Test {

    fun test() {
        println(Common.csApi.getStockQuote("SPY"))
        println(Common.csApi.getStockQuote("AAPL"))
    }


}

fun main() {
    Test().test()
}