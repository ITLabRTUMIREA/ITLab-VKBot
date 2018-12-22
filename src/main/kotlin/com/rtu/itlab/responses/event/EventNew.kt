package com.rtu.itlab.responses.event

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.*
import org.slf4j.LoggerFactory

/**
 * Class of sending a message to VC when a new event was created
 * @param eventView
 * @param db - Database with persons info

 */
class EventNew(private val eventView: EventView, db: DBClient) : ResponseHandler(db) {

    private val logger = LoggerFactory.getLogger("com.rtu.itlab.responses.event.EventNew")

    override fun send(): JsonObject {


        if (!userIds!!.isEmpty()) {

            val invitedUserIds = when (db) {
                null -> null
                else -> mutableSetOf<Int>(Gson().fromJson(db.getUsersVkIdForVkMailing(eventView.invited()).
                    getAsJsonArray("vkIDs"), object : TypeToken<Set<Int>>(){}.type))
            }

            val userIdsWithoutInvite: List<Int>

            if (!invitedUserIds.isNullOrEmpty()) {
                userIdsWithoutInvite = userIds.subtract(invitedUserIds).toList()
                EventInvite(eventView, db).send(invitedUserIds)
            } else {
                userIdsWithoutInvite = userIds.toList()
            }

            logger.info("Invited users: ${invitedUserIds!!.toList()}")
            logger.info("Not invited users: ${userIdsWithoutInvite.toList()}")

            vk.messages()
                .send(actor, userIdsWithoutInvite)
                .message(
                    "Было создано новое событие!\n«${eventView.title}»" +
                            "\nНеобходимое количество участников: ${eventView.targetParticipantsCount()}" +
                            "\nНачало: ${eventView.beginTime()}" +
                            "\nОкончание: ${eventView.endTime()}" +
                            "\nАдрес проведения мероприятия: ${eventView.address}" +
                            "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}"
                )
                .execute()
            resultJson.addProperty("statusCode", 1)
            logger.info("Info messages about new event sent to users VK")
        } else {
            resultJson.addProperty("statusCode", 30)
            logger.error("Can't send messages to users,userList for vkNotification is empty!")
        }
        return resultJson
    }
}