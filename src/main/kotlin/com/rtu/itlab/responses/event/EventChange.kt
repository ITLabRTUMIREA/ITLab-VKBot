package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient

/**
 * The class for notifying the user about changes in event
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventChange(tmp: JsonObject?, db: DBClient? = null) : EventInfo(tmp, db) {
    //TODO: Refine what happens when an event changes
    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Событие «${eventTitle}» было изменено!" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }

    fun send(userId: Int) {
        vk.messages()
                .send(actor, userId)
                .message("Событие «${eventTitle}» было изменено!" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }
}