package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient

/**
 * Class of sending a message to VC when a new event was created
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventNew(tmp: JsonObject?, db: DBClient) : EventInfo(tmp, db) {

    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Было создано новое событие!\n«${eventTitle}»" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }
}