package com.rtu.itlab.responses

import com.rtu.itlab.database.DBClient
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import java.io.File

abstract class ResponseHandler(val db: DBClient? = null) {
    private val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)
    val file = File("application.conf")

    val config = ConfigFactory.parseFile(file)
    val actor = GroupActor(config.getInt("group.id"), config.getString("group.accessToken"))

    val userIds = when (db) {
        null -> null
        else -> db.getUsersVkIdForVkMailing().toList()
    }

    abstract fun send()
}