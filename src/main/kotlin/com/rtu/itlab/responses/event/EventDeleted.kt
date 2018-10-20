package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.vk.api.sdk.client.actors.GroupActor

/**
 * Sending a message to the user about the cancellation of the event
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventDeleted(tmp: JsonObject?, db: DBClient? = null) : EventInfo(tmp, db) {

    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Событие «${eventTitle}» было удалено(отменено)!" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url")
                .execute()
    }
}