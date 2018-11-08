package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.*

/**
 * Class of sending a message to the user who was invited to the event.
 * @param tmp - Json info about event
 * @param db - Database with persons info
 */
class EventInvite(private val eventInviteView: EventInviteView, db: DBClient) : ResponseHandler(db) {

    override fun send() {
        vk.messages()
                .send(actor, userIds)
                .message("Вы были приглашены на событие\n«${eventInviteView.title}»" +
                        "\nНачало: ${eventInviteView.beginTime}" +
                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventInviteView.id}" +
                        "\nСвое участие можете подтвердить в личном кабинете по ссылке: https://itlab.azurewebsites.net/notifications")
                .execute()
    }

    fun send(userId: Int) {
        vk.messages()
                .send(actor, userId)
                .message("Вы были приглашены на событие\n«${eventInviteView.title}»" +
                        "\nНачало: ${eventInviteView.beginTime}" +
                        "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventInviteView.id}" +
                        "\nСвое участие можете подтвердить в личном кабинете по ссылке: https://itlab.azurewebsites.net/notifications")
                .execute()
    }
}