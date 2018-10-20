package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient

/**
 * Class of sending a message to the user who was invited to the event.
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventInvite(tmp: JsonObject?, db: DBClient? = null) : EventInfo(tmp, db) {

    //TODO: Clarify how the invitee id will be obtained

    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Вы были приглашены на событие\n«${eventTitle}»" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url" +
                        "\nСвое участие можете подтвердить в личном кабинете по ссылке: https://itlab.azurewebsites.net/notifications")
                .execute()
    }

    fun send(userId: Int) {
        vk.messages()
                .send(actor, userId)
                .message("Вы были приглашены на событие\n«${eventTitle}»" +
                        "\nНеобходимое количество участников: $participantsCount" +
                        "\nНачало: $beginDate $beginTime" +
                        "\nОкончание: $endDate $endTime" +
                        "\nАдрес проведения мероприятия: $address" +
                        "\nСсылка на событие: $url" +
                        "\nСвое участие можете подтвердить в личном кабинете по ссылке: https://itlab.azurewebsites.net/notifications")
                .execute()
    }
}