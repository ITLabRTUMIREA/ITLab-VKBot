package com.rtu.itlab.responses

import com.google.gson.JsonObject

class GetUserId(tmp: JsonObject?) : ResponseHandler(){
    val userId = tmp?.get("to")?.asInt

    override fun send() {
    }
}