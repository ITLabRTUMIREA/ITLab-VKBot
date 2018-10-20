package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient

/**
 * Sending a reminder to the user about an upcoming event
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventReminder(tmp: JsonObject?, db: DBClient? = null) : EventInfo(tmp, db) {
    //TODO: Clarify whether you need to make a notification system in a few days.
    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Напоминаем вам, что скоро состоится событие «${eventTitle}»" +
                        "\nНа данный момент еще необходимо участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }

    fun send(userId: Int) {
        vk.messages()
                .send(actor, userId)
                .message("Напоминаем вам, что скоро состоится событие «${eventTitle}»" +
                        "\nНа данный момент еще необходимо участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }
}