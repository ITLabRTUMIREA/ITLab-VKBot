package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.vk.api.sdk.client.actors.GroupActor

class EquipmentAdded (tmp: JsonObject?): ResponseHandler(){
    private val userId = tmp?.get("to")?.asInt
    private val equipment = tmp?.getAsJsonObject("equipment")?.get("title")?.asString
    private val actor = GroupActor(properties.getProperty("group.id").toInt(), properties.getProperty("group.accessToken"))

    override fun send(){
        vk.messages()
                .send(actor)
                .userId(userId)
                .message("За вами было закреплено новое оборудование:\n$equipment")
                .execute()
    }
}