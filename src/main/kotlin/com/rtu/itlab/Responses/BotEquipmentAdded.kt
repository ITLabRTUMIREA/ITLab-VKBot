package com.rtu.itlab.Responses

import com.google.gson.JsonObject
import com.rtu.itlab.Utils.getProp
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient

class BotEquipmentAdded (tmp: JsonObject?){
    private val transportClient = HttpTransportClient.getInstance()
    private val vk = VkApiClient(transportClient)
    private val properties = getProp()

    private val userId = tmp?.get("to")?.asInt
    private val equipment = tmp?.getAsJsonObject("equipment")?.get("title")?.asString
    private val actor = GroupActor(properties.getProperty("group.id").toInt(), properties.getProperty("group.accessToken"))

    fun send(){
        vk.messages()
                .send(actor)
                .userId(userId)
                .message("За вами было закреплено новое оборудование:\n$equipment")
                .execute()
    }
}