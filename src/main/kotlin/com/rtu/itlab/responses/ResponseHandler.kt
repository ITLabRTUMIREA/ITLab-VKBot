package com.rtu.itlab.responses

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.httpclient.HttpTransportClient

abstract class ResponseHandler {
    private val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)
    val config: Config = ConfigFactory.load()

    abstract fun send()
}