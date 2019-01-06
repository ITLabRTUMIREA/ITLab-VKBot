package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
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

            val notify = NotifyMessages().event().eventNew(eventView.title).eventInfo(eventView)

            val users = Users(eventView)

            logger.info("Invited users: ${users.invitedUsers}")
            logger.info("Not invited users: ${users.notInvitedUsers}")

            if (users.invitedUsers.isNotEmpty()) {
                EventInvite().send(users.invitedUsers, notify)
            }

            if (users.notInvitedUsers.isNotEmpty())

                vk.messages()
                    .send(actor, users.notInvitedUsers)
                    .message(
                        notify.concatenate()
                    ).execute()

            resultJson.addProperty("statusCode", 1)
            logger.info("Info messages about new event sent to users VK")
        } else {
            resultJson.addProperty("statusCode", 30)
            logger.error("Can't send messages to users,userList for vkNotification is empty!")
        }
        return resultJson
    }
}