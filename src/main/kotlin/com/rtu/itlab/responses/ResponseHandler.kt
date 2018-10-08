package com.rtu.itlab.responses

import com.rtu.itlab.utils.getProp
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.httpclient.HttpTransportClient

abstract class ResponseHandler {
    private val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)
    val properties = getProp()

    abstract fun send()
}