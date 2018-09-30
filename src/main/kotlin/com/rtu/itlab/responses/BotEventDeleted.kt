package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.rtu.itlab.utils.getProp
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient

class BotEventDeleted (tmp: JsonObject?){
    private val transportClient = HttpTransportClient.getInstance()
    private val vk = VkApiClient(transportClient)
    private val properties = getProp()

    private val userId = tmp?.get("to")?.asInt
    private val eventTitle: String? = tmp?.get("eventTitle")?.asString
    private val address: String? = tmp?.get("address")?.asString
    private val actor = GroupActor(properties.getProperty("group.id").toInt(), properties.getProperty("group.accessToken"))

    fun send(){
        vk.messages()
                .send(actor)
                .userId(userId)
                .message("Событие, на которое вы подписаны, было ОТМЕНЕНО\n$eventTitle\nАдрес: $address\nПриносим свои извинения")
                .execute()
    }
}