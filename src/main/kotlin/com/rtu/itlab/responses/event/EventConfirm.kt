package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient

/**
 * The class for notifying the user that his participation in the event is confirmed
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventConfirm(tmp: JsonObject?, db: DBClient? = null) : EventInfo(tmp, db) {

    //TODO: Specify how id will be received
    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Ваше участие в собитии «${eventTitle}» было подтверждено!" +
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
                .message("Ваше участие в собитии «${eventTitle}» было подтверждено!" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }
}