package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.EventView
import com.rtu.itlab.responses.event.models.beginTime
import com.rtu.itlab.responses.event.models.endTime
import com.rtu.itlab.responses.event.models.targetParticipantsCount

/**
 * The class for notifying the user about changes in event
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventChange(val eventView: EventView, db: DBClient) : ResponseHandler(db)  {
    //TODO: Refine what happens when an event changes
    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Событие «$${eventView.title}» было изменено!" +
                        "\nНеобходимое количество участников: ${eventView.targetParticipantsCount()}" +
                        "\nНачало: ${eventView.beginTime()}" +
                        "\nОкончание: ${eventView.endTime()}" +
                        "\nАдрес проведения мероприятия: ${eventView.address}" +
                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}")
                .execute()
    }

    fun send(userId: Int) {
        vk.messages()
                .send(actor, userId)
                .message("Событие «$${eventView.title}» было изменено!" +
                        "\nНеобходимое количество участников: ${eventView.targetParticipantsCount()}" +
                        "\nНачало: ${eventView.beginTime()}" +
                        "\nОкончание: ${eventView.endTime()}" +
                        "\nАдрес проведения мероприятия: ${eventView.address}" +
                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}")
                .execute()
    }
}