package com.rtu.itlab.responses

import com.google.gson.JsonObject

class GetVkToken(tmp: JsonObject?) : ResponseHandler(){
    val vkId = tmp?.getAsJsonObject("object")?.get("from_id")?.asInt
    val token: String = tmp!!.getAsJsonObject("object").get("text").asString

    override fun send() {
    }
}