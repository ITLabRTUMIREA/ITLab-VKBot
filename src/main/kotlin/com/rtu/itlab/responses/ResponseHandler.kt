package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
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

    //var actor = GroupActor(config.getInt("group.id"), authResponse.accessTokens[config.getInt("group.id")])


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

    abstract fun send(): JsonObject
}