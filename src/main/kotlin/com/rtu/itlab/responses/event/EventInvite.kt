package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.*
import org.slf4j.LoggerFactory

/**
 * Class of sending a message to the user who was invited to the event.
 * @param eventView - Json info about event
 * @param db - Database with persons info
 */
class EventInvite(private val eventView: EventView? = null, db: DBClient? = null) : ResponseHandler(db) {

    private val logger = LoggerFactory.getLogger("com.rtu.itlab.responses.event.EventInvite")

    override fun send(): JsonObject {
        return JsonObject()
    }

    fun send(invitedUsers: List<Int>, event: Event) {
        //if (!userIds!!.isEmpty()) {

            vk.messages()
                .send(actor, invitedUsers)
                .message(
                    event.invite().concatenate()
                ).execute()

            logger.info("Invite messages sent to users VK")
//        } else {
//            logger.error("Can't send messages to users,userList for vkNotification is empty!")
//        }
    }
}