package com.github.wigggy.botsbase.systems.bot_tools

import com.github.wigggy.botsbase.tools.FileHelper
import com.github.wigggy.charles_schwab_api.CharlesSchwabApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.text.SimpleDateFormat
import java.util.*

import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException


object Common {

    val gson = initGson()

    val csApi = initCsApi()


    private fun initCsApi(): CharlesSchwabApi {
        val x = FileHelper.readFileToString("botsbase\\src\\main\\resources\\cs_auth.json")
        val m = gson.fromJson(x, Map::class.java)
        return CharlesSchwabApi.buildApi(
            m["key"].toString(),
            m["secret"].toString(),
            "_savedata\\csapi_auth.json"
        )
    }


    private fun initGson(): Gson {
        return GsonBuilder()

            // A custom 'Date' converter is needed to go from Jul 01, 2023 -> 2023-07-01
            .registerTypeAdapter(
                Date::class.java,

                object : JsonDeserializer<Date> {
                    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

                    override fun deserialize(
                        json: JsonElement,
                        typeOfT: Type,
                        context: JsonDeserializationContext
                    ): Date? {
                        return try {
                            dateFormat.parse(json.asString)
                        } catch (e: ParseException) {
                            throw JsonParseException(e)
                        }
                    }
                }


            )
            .create()
    }


}
