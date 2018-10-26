package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.EventView
import com.vk.api.sdk.client.actors.GroupActor

/**
 * Sending a message to the user about the cancellation of the event
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventDeleted(val eventView: EventView, db: DBClient) : ResponseHandler(db){

    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Событие «${eventView.title}» было удалено(отменено)!")
                .execute()
    }
}