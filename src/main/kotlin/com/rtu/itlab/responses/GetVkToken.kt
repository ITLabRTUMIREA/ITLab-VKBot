package com.rtu.itlab.responses

import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.utils.UserCard
import com.vk.api.sdk.client.actors.GroupActor

class GetVkToken(tmp: JsonObject?) : ResponseHandler(){
    val vkId = tmp!!.getAsJsonObject("object").get("from_id").asInt
    val token: String = tmp!!.getAsJsonObject("object").get("text").asString

    override fun send() {
        if (token.startsWith("L:")) {
            Fuel.post("https://httpbin.org/post").body(Gson().toJson(UserCard(token, vkId))).header("Content-Type" to "application/json")
        } else {
            val actor = GroupActor(properties.getProperty("group.id").toInt(), properties.getProperty("group.accessToken"))
            vk.messages()
                    .send(actor)
                    .userId(vkId)
                    .message("Если вы пытались прислать код для верификация, товы сделали это как-то не так\n Проверьте правильность написания кода")
                    .execute()
        }
    }
}