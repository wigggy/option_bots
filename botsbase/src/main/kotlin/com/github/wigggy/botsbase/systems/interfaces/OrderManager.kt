package com.github.wigggy.botsbase.systems.interfaces

import com.github.wigggy.botsbase.systems.data.data_objs.OptionPosition



interface OrderManager {


    fun buyOrder(
        botName: String,
        bot_desc: String,
        option_symbol: String,
        quantity: Int,
        tp_dollar: Double,
        tp_pct: Double,
        stop_dollar: Double,
        stop_pct: Double,
        extra_data: Map<String, String>
    ): OptionPosition?


    fun sellOrder(
        optionPos: OptionPosition,
        closeReason: String,
        extraClosingData: String = "NONE"
    ): OptionPosition?


}