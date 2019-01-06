package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.event.EventInvite
import com.rtu.itlab.responses.event.NotifyMessages
import com.rtu.itlab.responses.event.models.EventView
import com.rtu.itlab.responses.event.models.invited
import com.rtu.itlab.utils.Config
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient


/**
 *Class for work with database, config, vkApi(Getting users Ids, vkApi)
 * @param db database object
 */
abstract class ResponseHandler(val db: DBClient? = null) {
    val config = Config().config!!
    private val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)

    /**
     * Status codes:
     * 1-OK
     *
     * 30 - Can't send messages to users,userList for vkNotification is empty!
     */
    val resultJson = JsonObject()

    val actor = GroupActor(config.getInt("group.id"), config.getString("group.accessToken"))

    val userIds = when (db) {
        null -> null
        else -> db.getUsersVkIdForVkMailing().getAsJsonArray("vkIDs").map { it.asInt }
    }


    /**
     * Class with info about invited and not invited users
     */
    inner class Users(eventView: EventView) {

        val invitedUsers = mutableListOf<Int>()
        val notInvitedUsers = mutableListOf<Int>()

        init {
            if (!userIds!!.isEmpty()) {

                val invitedUserIds = when (db) {
                    null -> null
                    else -> db.getUsersVkIdForVkMailing(eventView.invited()).getAsJsonArray("vkIDs").map { it.asInt }
                }
                //TODO: PRINT
                println(invitedUserIds)

                val userIdsWithoutInvite: List<Int>

                if (!invitedUserIds.isNullOrEmpty()) {
                    userIdsWithoutInvite = userIds.subtract(invitedUserIds).toList()
                    invitedUsers.addAll(invitedUserIds)
                } else {
                    userIdsWithoutInvite = userIds.toList()
                }
                notInvitedUsers.addAll(userIdsWithoutInvite)
            }
        }
    }

    abstract fun send(): JsonObject
}