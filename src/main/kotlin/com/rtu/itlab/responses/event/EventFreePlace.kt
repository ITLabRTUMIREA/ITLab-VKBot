package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.vk.api.sdk.client.actors.GroupActor

class EventFreePlace(tmp: JsonObject?):ResponseHandler() {
    //private val userIds = db.getUsersVkIdForVkMailing().toList()
    private val userIds = listOf(52495468) //TODO Кому отправлять. Здесь должна быть масс рассылка из бд
    private val eventTitle = tmp?.getAsJsonObject("data")?.get("title")?.asString
    private val address = tmp?.getAsJsonObject("data")?.get("address")?.asString
    private val participantsCount = ((tmp?.getAsJsonObject("data")?.get("targetParticipantsCount")!!.asInt) -
            (tmp.getAsJsonObject("data")?.get("currentParticipantsCount")!!.asInt)).toString()
    private val actor = GroupActor(config.getInt("group.id"), config.getString("group.accessToken"))

    private val beginTime = tmp?.getAsJsonObject("data")?.get("beginTime")?.asString
    private val endTime = tmp?.getAsJsonObject("data")?.get("endTime")?.asString
    private val url = "https://itlab.azurewebsites.net/events/" + tmp?.getAsJsonObject("data")?.get("id")?.asString

    override fun send() {
        val beginDate = beginTime!!.split("T")[0].replace("-", ".")
        val beginTime = beginTime.split("T")[1].dropLast(1)

        val endDate = endTime!!.split("T")[0].replace("-", ".")
        val endTime = endTime.split("T")[1].dropLast(1)

        vk.messages()
                .send(actor, userIds)
                .message("На событие ІІ $eventTitle » появились свободные места!\n" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }
}