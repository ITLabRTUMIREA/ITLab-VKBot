package com.rtu.itlab

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.database.DBUser
import com.rtu.itlab.responses.*
import com.rtu.itlab.responses.event.*
import com.rtu.itlab.responses.event.models.EventView
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
import kotlin.concurrent.timer

private lateinit var db: DBClient
private val logger = LoggerFactory.getLogger("com.rtu.itlab.Server")

fun Application.main() {
    val config = Config("application.conf").config!!
    db = DBClient()

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
    }

    routing {
        get("/") { call.respondText { "It's NOT OK" } }

        post("/bot") {
            val tmp: JsonObject? =
                Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)

            when (tmp!!.get("type").asString) {

                "EventNew" -> {
                    logger.info("Request for new event.")
                    call.respond(EventNew(Gson().fromJson(tmp.get("data"), EventView::class.java), db).send())
                }

                "EventInvite" -> {
                    EventInvite(Gson().fromJson(tmp, EventView::class.java), db).send()
                }

                "EquipmentAdded" -> {
                    EquipmentAdded(tmp).send()
                }
                "EventChange" -> {
                    EventChange(Gson().fromJson(tmp, EventView::class.java), db).send()
                }
                "EventConfirm" -> {
                    EventConfirm(Gson().fromJson(tmp, EventView::class.java), db).send()
                }
                "EventDenied" -> {
                    EventDenied(Gson().fromJson(tmp, EventView::class.java), db).send()
                }
                "EventDeleted" -> {
                    EventDeleted(Gson().fromJson(tmp, EventView::class.java), db).send()
                }
                "EventExcluded" -> {
                    EventExcluded(Gson().fromJson(tmp, EventView::class.java), db).send()
                }
                "EventFreePlace" -> {
                    //EventFreePlace(tmp, db).send()
                }


                "EventReminder" -> {
                    EventReminder(Gson().fromJson(tmp, EventView::class.java), db).send()
                }

                "confirmation" -> {
                    call.respond(config.getString("server.response"))
                    // VK synergy
                }


                "message_new" -> {
                    GetVkToken(tmp, db).send()
                    call.respond("OK") // Code Handler
                }

                else -> call.respondText { "It's Ok, just Wrong" }
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

        delete("/bot/db/person/delete/{id}") {
            call.respond(db.deletePerson(call.parameters["id"]))
        }

        delete("/bot/db/persons/delete") {
            call.respond(db.deleteAllPersons())
        }

        get("/bot/db/isconnected") {
            val result = JsonObject()
            result.addProperty("connection", db.isConnected())
            call.respond(result)
        }

    }
}
