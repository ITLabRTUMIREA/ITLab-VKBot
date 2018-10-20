package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient

/**
 * The class for notifying the user that he was suspended from participating in an event
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventExcluded(tmp: JsonObject?, db: DBClient? = null) : EventInfo(tmp, db) {

    //TODO: Specify how id will be received 3
    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Вы были отстранены от участия в событии «${eventTitle}»" +
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
                .message("Вы были отстранены от участия в событии «${eventTitle}»" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }
}