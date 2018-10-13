package com.rtu.itlab

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.responses.*
import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.*
import io.ktor.request.receive
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
            val tmp: JsonObject? = Gson().fromJson(InputStreamReader(call.receiveStream(),"UTF-8"), JsonObject::class.java) // ПРИМЕР ТОГО, ЧТО ТОЧНО РАБОТАЕТ КАК НАДО
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
                    EventFreePlace(tmp).send()
                }
                "EventInvite" -> {
                    EventInvite(tmp).send()
                }
                "EventNew" -> {
                    EventNew(tmp).send()
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


        post("/person/add") {
            val tmp: JsonObject = Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)
            db!!.addPerson(tmp)
            call.respond("OK")
        }

        post("/person/get") {
            val tmp: JsonObject = Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)
            call.respond(db.getUserInfoByKey(tmp))
        }

        get("/persons/get") {
            call.respond(db.getAllPersons()!!)
        }

        post("/person/delete") {
            val tmp: JsonObject = Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)
            db.deletePerson(tmp)
            call.respond("OK")
        }


//            val tmp: JsonObject? = Gson().fromJson(InputStreamReader(call.receiveStream(),"UTF-8"), JsonObject::class.java)  ПРИМЕР ТОГО, ЧТО ТОЧНО РАБОТАЕТ КАК НАДО
    }
}
