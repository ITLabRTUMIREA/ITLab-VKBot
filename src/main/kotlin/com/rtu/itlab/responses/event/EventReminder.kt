package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.EventView
import com.rtu.itlab.responses.event.models.beginTime
import com.rtu.itlab.responses.event.models.endTime
import com.rtu.itlab.responses.event.models.targetParticipantsCount

/**
 * Sending a reminder to the user about an upcoming event
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventReminder(val eventView: EventView, db: DBClient) : ResponseHandler(db) {
    //TODO: Clarify whether you need to make a notification system in a few days.
    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Напоминаем вам, что скоро состоится событие «${eventView.title}»" +
                        "\nНа данный момент еще необходимо участников: ${eventView.targetParticipantsCount()}" +
                        "\nНачало: ${eventView.beginTime()}" +
                        "\nОкончание: ${eventView.endTime()}" +
                        "\nАдрес проведения мероприятия: ${eventView.address}" +
                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}")
                .execute()
    }

    fun send(userId: Int) {
        vk.messages()
                .send(actor, userId)
                .message("Напоминаем вам, что скоро состоится событие «${eventView.title}»" +
                        "\nНа данный момент еще необходимо участников: ${eventView.targetParticipantsCount()}" +
                        "\nНачало: ${eventView.beginTime()}" +
                        "\nОкончание: ${eventView.endTime()}" +
                        "\nАдрес проведения мероприятия: ${eventView.address}" +
                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}")
                .execute()
    }
}