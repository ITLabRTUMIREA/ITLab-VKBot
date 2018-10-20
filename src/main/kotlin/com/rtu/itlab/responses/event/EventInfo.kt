package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.vk.api.sdk.client.actors.GroupActor

/**
 * A class with all the necessary information to send to users.
 * @param tmp - Json with info about event
 * @param db - Database with persons info
 */
abstract class EventInfo(val tmp: JsonObject?, db: DBClient? = null) : ResponseHandler() {

    //TODO: To start sending messages on DB uncomment the lines
//    val userIds = when (db) {
//        null -> null
//        else -> db?.getUsersVkIdForVkMailing().toList()
//    }

    val userIds = listOf(52495468)
    val eventTitle = tmp?.getAsJsonObject("data")?.get("title")?.asString
    val address = tmp?.getAsJsonObject("data")?.get("address")?.asString
    val participantsCount = ((tmp?.getAsJsonObject("data")?.get("targetParticipantsCount")!!.asInt) -
            (tmp.getAsJsonObject("data")?.get("currentParticipantsCount")!!.asInt)).toString()
    val actor = GroupActor(config.getInt("group.id"), config.getString("group.accessToken"))

    private val beginAllInfoTime = tmp?.getAsJsonObject("data")?.get("beginTime")?.asString
    private val endAllInfoTime = tmp?.getAsJsonObject("data")?.get("endTime")?.asString
    val url = "https://itlab.azurewebsites.net/events/" + tmp?.getAsJsonObject("data")?.get("id")?.asString

    val beginDate = beginAllInfoTime!!.split("T")[0].replace("-", ".")
    val beginTime = beginAllInfoTime!!.split("T")[1].dropLast(1)

    val endDate = endAllInfoTime!!.split("T")[0].replace("-", ".")
    val endTime = endAllInfoTime!!.split("T")[1].dropLast(1)
}