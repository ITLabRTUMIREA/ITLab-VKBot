package com.rtu.itlab

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.bot.keyboard.getKeyboardForCurrentPerson
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.database.DBUser
import com.rtu.itlab.responses.VKMessageHandling
import com.rtu.itlab.responses.event.*
import com.rtu.itlab.responses.event.models.EventView
import com.rtu.itlab.responses.event.models.NotifyType
import com.rtu.itlab.utils.Config
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.*
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import java.io.InputStreamReader
import org.slf4j.LoggerFactory

private lateinit var db: DBClient
private val logger = LoggerFactory.getLogger("com.rtu.itlab.Server")

fun Application.main() {
    Config().companion.pathToConfFile = "application.conf"

    db = DBClient()

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
    }

    routing {
        get("/") { call.respond("It's OK") }

        post("/bot") {
            val tmp: JsonObject? =
                Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)

            if (!tmp!!.get("type").asString.matches(Regex("[0-9]*"))) {

                when (tmp.get("type").asString) {
                    "confirmation" -> {
                        val config = Config().config!!
                        call.respond(config.getString("server.response"))
                    }
                    "message_new" -> {
                        VKMessageHandling(tmp, db).send()
                        call.respond("OK")
                    }
                    "message_reply" -> {
                        call.respond("OK")
                    }
                }

            } else {
                val type = tmp.get("type").asInt
                if (type <= NotifyType.values().size - 1) {
                    when (NotifyType.values()[tmp.get("type").asInt]) {

                        NotifyType.EventNew -> {
                            logger.info("Request for new event.")
                            call.respond(EventNew(Gson().fromJson(tmp.get("data"), EventView::class.java), db).send())
                        }

                        NotifyType.EventChange -> {
                            logger.info("Request for changed event")
                            call.respond(
                                EventChange(
                                    Gson().fromJson(tmp.get("data"), EventView::class.java),
                                    db
                                ).send()
                            )
                        }

                        NotifyType.EventConfirm -> {
                            logger.info("Request for changed event")
                            val userId = tmp.get("data").asJsonObject.get("user").asJsonObject.get("id").asString
                            call.respond(
                                EventConfirm(
                                    Gson().fromJson(tmp.get("data"), EventView::class.java),
                                    db,
                                    userId
                                ).send()
                            )
                        }

                        else -> call.respondText { "Wrong type" }
                    }
                } else {
                    call.respond("Error number of NotifyType")
                }
            }
        }

        post("/bot/db") {
            val tmp: JsonObject? =
                Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)

            when (tmp!!.get("type").asString) {

                "disconnect" -> {
                    db.closeConnection()
                    val result = JsonObject()
                    result.addProperty("statusCode", 1)
                    logger.info("Disconnecting from database")
                    call.respond(result)
                }

                "addPerson" -> {
                    call.respond(db.addPerson(tmp.getAsJsonObject("data")))
                }

                "personUpdate" -> {
                    call.respond(db.updatePersonInfo(tmp.getAsJsonObject("data")))
                }

                "addPersons" -> {
                    logger.info("Request for adding person.")
                    call.respond(
                        db.addPersons(
                            Gson().fromJson(
                                tmp.getAsJsonArray("data"),
                                Array<DBUser>::class.java
                            )
                        )
                    )
                }

            }
        }

        get("bot/db/persons/ispersonindbbyvkid/{vkid}") {
            call.respond(db.isUserInDBByVkId(call.parameters["vkid"]!!.toInt()))
        }

        get("/bot/db/persons/get") {
            call.respond(db.getAllPersons())
        }

        get("/bot/db/persons/mailnotice") {
            call.respond(db.getUsersMailsForEmailMailing())
        }

        get("/bot/db/persons/phonenotice") {
            call.respond(db.getUsersPhonesForPhoneMailing())
        }

        get("/bot/db/persons/vknotice") {
            call.respond(db.getUsersVkIdForVkMailing())
        }

        get("/bot/db/person/{id}") {
            call.respond(db.getUserInfoByKey(call.parameters["id"]))
        }

        get("/bot/db/dump") {
            call.respond(db.makeDump())
        }

        get("/bot/keyboard/{vkId}") {
            call.respond(getKeyboardForCurrentPerson(call.parameters["vkId"]!!.toInt(), db).getKeyboardJson())
        }

        delete("/bot/db/person/delete/{id}") {
            call.respond(db.deletePerson(call.parameters["id"]))
        }

        delete("/bot/db/persons/delete") {
            call.respond(db.deleteAllPersons())
        }

        get("/bot/db/connect") {
            call.respond(db.loadConfigAndConnect())
        }

        get("/bot/db/isconnected") {
            val result = JsonObject()
            result.addProperty("connection", db.isConnected())
            call.respond(result)
        }

    }
}
