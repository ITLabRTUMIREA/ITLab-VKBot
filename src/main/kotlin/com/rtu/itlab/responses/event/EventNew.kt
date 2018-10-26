package com.rtu.itlab.responses.event

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.EventView
import com.rtu.itlab.responses.event.models.beginTime
import com.rtu.itlab.responses.event.models.endTime
import com.rtu.itlab.responses.event.models.targetParticipantsCount
import java.beans.EventHandler

/**
 * Class of sending a message to VC when a new event was created
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventNew(val eventView: EventView, db: DBClient) : ResponseHandler(db) {

    override fun send() {

        vk.messages()
                .send(actor, userIds)
                .message("Было создано новое событие!\n«${eventView.title}»" +
                        "\nНеобходимое количество участников: ${eventView.targetParticipantsCount()}" +
                        "\nНачало: ${eventView.beginTime()}" +
                        "\nОкончание: ${eventView.endTime()}" +
                        "\nАдрес проведения мероприятия: ${eventView.address}" +
                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}")
                .execute()
    }
}