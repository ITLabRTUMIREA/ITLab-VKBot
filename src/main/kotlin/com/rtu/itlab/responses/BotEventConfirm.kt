package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.rtu.itlab.utils.getProp
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient

fun notifyAboutEventConfirm(tmp: JsonObject?) {

    //TODO: USER WAS APPLIED TO EVENT OR NO

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
            .message("Ваше участие в собитии было подтверждено!\n$eventTitle\nАдрес: $address")
            .execute()
}