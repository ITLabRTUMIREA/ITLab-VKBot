package com.rtu.itlab

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.Utils.getProp
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.*
import io.ktor.request.receiveStream
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import java.io.InputStreamReader

fun Application.main() {

    val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)
    val properties = getProp()

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        post("/") {

            val tmp: JsonObject? = Gson().
                    fromJson(InputStreamReader(call.receiveStream(),"UTF-8"), JsonObject::class.java)

            val type: String? = tmp?.get("type")?.asString

            val body: String? = tmp?.getAsJsonObject("object")?.get("text")?.asString

            val user_id = tmp?.getAsJsonObject("object")?.get("from_id")?.asInt

            val actor = GroupActor(properties.getProperty("group.id").toInt(), properties.getProperty("group.accessToken"))

            if (type.equals("message_new")) {
                vk.messages()
                        .send(actor)
                        .userId(user_id)
                        .message("DmRomanov Server: Привет! $body")
                        .execute()
                call.respond("ok")
            } else {
                if (type.equals("confirmation")) call.respondText(properties.getProperty("server.response"))
                else {
                    call.respondText("ok")
                }
            }

        }

        get("/") { call.respondText { "It's OK, just Wrong" } }
    }
}
