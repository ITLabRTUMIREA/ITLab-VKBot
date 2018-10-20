package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.vk.api.sdk.client.actors.GroupActor

class EventInvite(tmp: JsonObject?):ResponseHandler() {
    private val userId = tmp?.get("to")?.asInt
    private val eventTitle: String? = tmp?.get("eventTitle")?.asString
    private val address: String? = tmp?.get("address")?.asString
    private val actor = GroupActor(config.getInt("group.id"), config.getString("group.accessToken"))

    override fun send() {
        vk.messages()
                .send(actor)
                .userId(userId)
                .message("Вы были приглашены к участию в событии!\n$eventTitle\nАдрес: $address")
                .execute()
    }
}