@file:Suppress("IMPLICIT_CAST_TO_ANY")

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import database.HibernateUtil
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import messageprocessing.VKMessageHandling
import org.slf4j.LoggerFactory
import rediswork.RedisListener
import utils.Config
import workwithapi.RequestsToServerApi
import java.io.InputStreamReader
import kotlin.concurrent.thread

@Suppress("requestHandler")
fun Application.module() {
    Config("resources/secureInfo.conf")
    Config().loadConfig()

    val databaseConnection = HibernateUtil().setUpSession()

    val logger = LoggerFactory.getLogger("HttpRequestHandler")
    val requestsToServerApi = RequestsToServerApi()
    val vkMessageHandling = VKMessageHandling(requestsToServerApi)
    val redisListener = RedisListener(databaseConnection, requestsToServerApi)

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
    }

    if (redisListener.jedis == null || !redisListener.jedis!!.isConnected)
        thread { redisListener.listenEvents() }

    routing {

        get("/loadconfig") {
            redisListener.unsubscribe()
            Config().loadConfig()

            if (redisListener.jedis == null || !redisListener.jedis!!.isConnected)
                thread { redisListener.listenEvents() }

            call.respond(HttpStatusCode.OK)
        }

        get("/") {
            call.respond("Server is online")
        }

        post("/bot") {
            try {
                val inputJson = Gson().fromJson(
                    InputStreamReader(
                        call.receiveStream(),
                        "UTF-8"
                    ), JsonObject::class.java
                )

                val secretFromConfigForVkApi = Config().loadPath("group.secret")

                val secretFromRequest = inputJson.get("secret").asString

                //If request from vk api
                if (!secretFromConfigForVkApi.isNullOrBlank() &&
                    !secretFromRequest.isNullOrBlank() &&
                    secretFromConfigForVkApi == secretFromRequest
                ) {

                    when (inputJson.get("type").asString) {

                        "confirmation" -> {
                            val responseForConnectServerToVk = Config().loadPath("group.responseForConnect")
                            if (responseForConnectServerToVk != null)
                                call.respond(responseForConnectServerToVk)
                            else {
                                logger.error("group.responseForConnect is null.")
                            }
                        }

                        "message_new" -> {

                            if (!inputJson.isJsonNull)
                                vkMessageHandling.process(inputJson, databaseConnection)
                            else
                                logger.error("Json from vk api is null")

                            call.respond("OK")
                        }

                        "message_reply" -> {
                            call.respond("OK")
                        }
                    }

                }

            } catch (jsonSyntaxEx: JsonSyntaxException) {
                logger.error("JsonSyntaxException ${jsonSyntaxEx.message}")
                call.response.status(HttpStatusCode.BadRequest)
            }
        }
    }

}




