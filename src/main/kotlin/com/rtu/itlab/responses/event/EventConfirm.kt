package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.EventView
import org.slf4j.LoggerFactory

class EventConfirm(private val eventView: EventView, db: DBClient, val id: String) : ResponseHandler(db) {

    private val logger = LoggerFactory.getLogger("com.rtu.itlab.responses.event.EventConfirm")

    override fun send(): JsonObject {
        val userInfo = db!!.getDbUserByKey(id)

        if (userInfo != null) {

            val notify = NotifyMessages().event().eventConfirm(eventView.title).addUrl(eventView)

            logger.info("User vkId: ${userInfo.vkId}")
            if (userInfo.vkNotice) {
                vk.messages()
                    .send(actor, userInfo.vkId!!.toInt())
                    .message(
                        notify.concatenate()
                    ).execute()
            }

            resultJson.addProperty("statusCode", 1)
            logger.info("Info messages about event confirm sent to user VK ${userInfo.vkNotice}")
        } else {
            resultJson.addProperty("statusCode", 30)
            logger.error("Can't send messages to user, can't find him in db!")
        }
        return resultJson
    }

}