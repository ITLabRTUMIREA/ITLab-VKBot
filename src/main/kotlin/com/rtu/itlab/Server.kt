package com.rtu.itlab

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.database.DB
import com.rtu.itlab.responses.*
import com.rtu.itlab.utils.UserCard
import com.rtu.itlab.utils.getProp
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.*
import io.ktor.request.receive
import io.ktor.request.receiveStream
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.vertx.core.Handler
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.time.delay
import kotlinx.io.core.String
import java.io.InputStreamReader

fun Application.main() {
    val db = DB("1230", null, null)
    var users = mutableListOf<UserCard>()

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        get("/") { call.respondText { "It's OK, just Wrong" } }

        post("/bot") {
            val tmp = call.receive<JsonObject>()//ПРОВЕРКА НЕОБХОДИМА   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            when (tmp.get("type").asString) {
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
                "VkVerification" -> {
                    users.add(UserCard(GetUserId(tmp).userId))
                    var x = false
                    while (!x) {
                        for (item in users) {
                            if (item.userId == item.vkId) {
                                call.respondText { item.token }
                                users.remove(item)
                                x = true
                                break
                            }
                        }
                    }
                }
            }
        }

        post("/") {
            val tmp = call.receive<JsonObject>()
            when {
                tmp.get("type").asString.equals("confirmation") -> call.respond(getProp().getProperty("server.response"))
                tmp.get("type").asString.equals("message_new") -> {
                    for (item in users) {
                        if (item.userId == GetVkToken(tmp).vkId) {
                            item.vkId = GetVkToken(tmp).vkId
                            item.token = GetVkToken(tmp).token
                        }
                    }
                    call.respond("ok")
                }
                else -> call.respond("ok")
            }
        }

        post("/person/add") {
            val tmp: JsonObject = Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)
            db.addPerson(io.vertx.core.json.JsonObject(tmp.toString()))
            call.respond("OK")
        }

        post("/person/get") {
            val tmp: JsonObject = Gson().fromJson(InputStreamReader(call.receiveStream(), "UTF-8"), JsonObject::class.java)
            call.respond(db.getUserInfoByKey(io.vertx.core.json.JsonObject(tmp.toString())))

        }

//        post("/person/update"){
//            val tmp: JsonObject = Gson().fromJson(InputStreamReader(call.receiveStream(),"UTF-8"), JsonObject::class.java)
//            val personInfo = io.vertx.core.json.JsonObject(tmp.toString())
//            var id = personInfo.getString("id")
//            //println(id)
//            tmp.remove("id")
//            db.updatePesonValue(id,tmp)
//        }
//
//        get("/persons/get"){
//            db.getAllPersons()
//        }

//            val tmp: JsonObject? = Gson().fromJson(InputStreamReader(call.receiveStream(),"UTF-8"), JsonObject::class.java)  ПРИМЕР ТОГО, ЧТО ТОЧНО РАБОТАЕТ КАК НАДО
    }
}
