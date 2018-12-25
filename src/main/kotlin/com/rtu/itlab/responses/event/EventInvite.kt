package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.*
import org.slf4j.LoggerFactory

/**
 * Class of sending a message to the user who was invited to the event.
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventInvite(private val eventView: EventView, db: DBClient?) : ResponseHandler(db) {
    private val logger = LoggerFactory.getLogger("com.rtu.itlab.responses.event.EventInvite")
    override fun send(): JsonObject {
//        vk.messages()
//            .send(actor, userIds)
//            .message(
//                "Вы были приглашены на событие\n«${eventInviteView.title}»" +
//                        "\nНачало: ${eventInviteView.beginTime}" +
//                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventInviteView.id}" +
//                        "\nСвое участие можете подтвердить в личном кабинете по ссылке: https://itlab.azurewebsites.net/notifications"
//            )
//            .execute()

        //vk.messages().send(actor,userIds).unsafeParam("")
        return JsonObject()
    }

    fun send(invitedUsers: Set<Int>) {
        if (!userIds!!.isEmpty()) {

//            val invitedUserIds = when (db) {
//                null -> null
//                else -> db.getUsersVkIdForVkMailing(eventView.invited())
//            }
//
//            if (!invitedUserIds.isNullOrEmpty()) {
//                userIds.minus(invitedUserIds.iterator())
//                EventInvite(eventView, db).send(invitedUserIds)
//            }

            vk.messages()
                .send(actor, invitedUsers.toList())
                .message(
                    "Вы были приглашены участвовать в событии !\n«${eventView.title}»" +
                            "\nНачало: ${eventView.beginTime()}" +
                            "\nОкончание: ${eventView.endTime()}" +
                            "\nАдрес проведения мероприятия: ${eventView.address}" +
                            "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}"
                )
                .execute()
            logger.info("Invite messages sent to users VK")
        } else {
            logger.error("Can't send messages to users,userList for vkNotification is empty!")
        }
    }

//fun send(userIds: Set<Int>) {
//    vk.messages()
//        .send(actor, userIds.toList())
//        .message(
//            "Вы были приглашены на событие\n«${eventInviteView.title}»" +
//                    "\nНачало: ${eventInviteView.beginTime}" +
//                    "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventInviteView.id}" +
//                    "\nСвое участие можете подтвердить в личном кабинете по ссылке: https://itlab.azurewebsites.net/notifications"
//        )
//        .execute()
//}
//
//fun send(userId: Int) {
//    vk.messages()
//        .send(actor, userId)
//        .message(
//            "Вы были приглашены на событие\n«${eventInviteView.title}»" +
//                    "\nНачало: ${eventInviteView.beginTime}" +
//                    "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventInviteView.id}" +
//                    "\nСвое участие можете подтвердить в личном кабинете по ссылке: https://itlab.azurewebsites.net/notifications"
//        )
//        .execute()
//}
}