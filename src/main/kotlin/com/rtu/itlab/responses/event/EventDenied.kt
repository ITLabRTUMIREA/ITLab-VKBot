package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.EventView

/**
 * The class for notifying the user that his participation in the event is denied
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventDenied(val eventView: EventView, db: DBClient) : ResponseHandler(db){

    //TODO: Specify how id will be received 2
    override fun send(): JsonObject{
        vk.messages()
                .send(actor, userIds)
                .message("Ваша заявка на участии на собитие «${eventView.title}»  отклонена!")
                .execute()
        return resultJson
    }

    fun send(userId: Int) {
        vk.messages()
                .send(actor, userId)
                .message("Ваша заявка на участии на собитие «${eventView.title}»  отклонена!")
                .execute()
    }
}