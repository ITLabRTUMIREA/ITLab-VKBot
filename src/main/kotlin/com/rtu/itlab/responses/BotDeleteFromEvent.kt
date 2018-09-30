package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.rtu.itlab.utils.getProp
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient

fun notifyAboutDeleteFromEvent(tmp: JsonObject?) {
    val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)

    val properties = getProp()

    val userId = tmp?.get("to")?.asInt
    val eventTitle: String? = tmp?.get("eventTitle")?.asString
    val address: String? = tmp?.get("address")?.asString
    val actor = GroupActor(properties.getProperty("group.id").toInt(), properties.getProperty("group.accessToken"))


    vk.messages()
            .send(actor)
            .userId(userId)
            .message("Вы была отстранены от участия в событии!\n$eventTitle\nАдрес: $address")
            .execute()
}