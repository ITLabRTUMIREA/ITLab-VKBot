package com.rtu.itlab.responses

import com.rtu.itlab.database.DBClient
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient

abstract class ResponseHandler(val db:DBClient? = null) {
    private val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)
    val config: Config = ConfigFactory.load()
    val actor =  GroupActor(config.getInt("group.id"), config.getString("group.accessToken"))

    //TODO: To start sending messages on DB uncomment the lines
    val userIds = when (db) {
        null -> null
        else -> db.getUsersVkIdForVkMailing().toList()
    }
    abstract fun send()
}