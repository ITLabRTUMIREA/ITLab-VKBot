package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.vk.api.sdk.client.actors.GroupActor

class EventNew(tmp: JsonObject?): ResponseHandler() {
    private val userId = 260397691//tmp?.get("to")?.asInt //TODO Кому отправлять. Здесь должна быть масс рассылка из бд
    private val eventTitle: String? = tmp?.getAsJsonObject("data")?.get("title")?.asString
    private val address: String? = tmp?.getAsJsonObject("data")?.get("address")?.asString
    private val actor = GroupActor(properties.getProperty("group.id").toInt(), properties.getProperty("group.accessToken"))

    override fun send() {
        vk.messages()
                .send(actor)
                .userId(userId)
                .message("Было создано новое событие!\n$eventTitle\nАдрес: $address")
                .execute()
    }
}