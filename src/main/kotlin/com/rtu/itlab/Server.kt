package com.rtu.itlab

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.*
import com.rtu.itlab.responses.event.*
import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.*
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import java.io.InputStreamReader

fun Application.main() {

    val config = ConfigFactory.load()
    val db = DBClient("1230")

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        get("/") { call.respondText { "It's OK, just Wrong" } }

        post("/bot") {
            val tmp: JsonObject? = Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java) // ПРИМЕР ТОГО, ЧТО ТОЧНО РАБОТАЕТ КАК НАДО
//            val tmp = call.receive<JsonObject>()//ПРОВЕРКА НЕОБХОДИМА   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            when (tmp!!.get("type").asString) {
                "EquipmentAdded" -> {
                    EquipmentAdded(tmp).send()
                }
                "EventChange" -> {
                    EventChange(tmp).send()
                }
                "EventConfirm" -> {
                    EventConfirm(tmp).send()
                }
                "EventDeleted" -> {
                    EventDeleted(tmp).send()
                }
                "EventExcluded" -> {
                    EventExcluded(tmp).send()
                }
                "EventFreePlace" -> {
                    EventFreePlace(tmp,db).send()
                }
                "EventInvite" -> {
                    EventInvite(tmp).send()
                }
                "EventNew" -> {
                    EventNew(tmp, db).send()
                }
                "EventRejected" -> {
                    EventRejected(tmp).send()
                }
                "EventReminder" -> {
                    EventReminder(tmp).send()
                }
                "confirmation" -> {
                    call.respond(config.getString("server.response"))
                    // VK synergy
                }
                "message_new" -> {
                    GetVkToken(tmp).send()
                    call.respond("ok") // Code Handler
                }
                else -> call.respondText { "It's Ok, just Wrong" }
            }
        }

        post("/bot/db") {

            val tmp: JsonObject? = Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)

            when (tmp!!.get("type").asString) {

                "addPerson" -> {
                    db!!.addPerson(tmp.getAsJsonObject("data"))
                    call.respond("OK")
                }

                "getPerson" -> {
                    call.respond(db.getUserInfoByKey(tmp.getAsJsonObject("data")))
                }

                "deletePerson" -> {
                    db.deletePerson(tmp.getAsJsonObject("data"))
                    call.respond("OK")
                }

                "personUpdate" -> {
                    call.respond(db.updatePersonInfo(tmp.getAsJsonObject("data")))
                }

                "addPersons" -> {
                    db.addPersons(tmp.getAsJsonObject("data"))
                    call.respond("OK")
                }
            }
        }

        get("/bot/db/persons/get") {
            call.respond(db.getAllPersons()!!)
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

        delete("/bot/db/persons/delete") {
            db.deleteAllPersons()
            call.respond("OK")
        }

    }
}
