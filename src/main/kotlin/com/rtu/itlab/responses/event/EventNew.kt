package com.rtu.itlab.responses.event

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
    val logger = LoggerFactory.getLogger("com.rtu.itlab.responses.event.EventNew")
    override fun send() {

        if (!userIds!!.isEmpty()) {

            val invitedUserIds = when (db) {
                null -> null
                else -> db.getUsersVkIdForVkMailing(eventView.invited())
            }

            if (!invitedUserIds.isNullOrEmpty()) {
                userIds.minus(invitedUserIds.iterator())
                EventInvite(eventView, db).send(invitedUserIds)
            }

            vk.messages()
                .send(actor, userIds)
                .message(
                    "Было создано новое событие!\n«${eventView.title}»" +
                            "\nНеобходимое количество участников: ${eventView.targetParticipantsCount()}" +
                            "\nНачало: ${eventView.beginTime()}" +
                            "\nОкончание: ${eventView.endTime()}" +
                            "\nАдрес проведения мероприятия: ${eventView.address}" +
                            "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}"
                )
                .execute()
            logger.info("Messages sent to users VK")
        } else {
            logger.error("Can't send messages to users,userList for vkNotification is empty!")
        }
    }
}