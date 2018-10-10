package com.rtu.itlab.database

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.redis.RedisOptions
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions

class DB {

    private var config: RedisOptions? = null
    private var redis: RedisClient? = null
    private val keyPattern = "[0-9a-z]*-[0-9a-z]*-" +
            "[0-9a-z]*-[0-9a-z]*-[0-9a-z]*"
    private val KEY = "id"

    constructor() {}

    /**
     * default ip = 127.0.0.1
     * default port = 6379
     */
    constructor(password: String?, ip: String?, port: Int?) {
        createConnectionToDB(password, ip, port)
    }

    /**
     * default ip = 127.0.0.1
     * default port = 6379
     */
    fun createConnectionToDB(password: String?, ip: String?, port: Int?) {

        config = RedisOptions(host = ip, port = port, auth = password)

        redis = RedisClient.create(Vertx.vertx(), config)

    }

    /**
     * Method for adding or updating person info if person with this id already exist
     * vkNotice,emailNotice,phoneNotice - boolean values
     * id,firstName,lastName,phoneNumber,email,vkId - string values
     * example of JsonObject
     * {
    "id": "123123341",
    "firstName": "Дмитрий",
    "lastName": "Романов",
    "phoneNumber": "+71111111111",
    "email": "qwe@gmail.com",
    "vkId": "vkId",
    "vkNotice": "true",
    "emailNotice": "true",
    "phoneNotice": "true"
     * }
     */
    fun addPerson(person: JsonObject) {
        val id = person.getString(KEY)
        person.remove(KEY)
        redis!!.hmset(id, person) { r ->
            if (r.succeeded()) {
                makeDump()
                println("Person added!")
            } else {
                println("Error adding person, check connection to database!")
            }
        }
    }

    fun makeDump() {
        redis!!.save { r ->
            if (r.succeeded()) {
                println("Dump was created/updated!")
            }
        }
    }


    fun getUserInfoByKey(person: JsonObject):JsonObject {
        var result = JsonObject()
        var b = true
//        var handleUserInfo = Handler<JsonObject> { info ->
//            result = info
//            b = false
//            return@Handler
//            //print("Result from handler $result")
//        }

        val id = person.getString(KEY)
        redis!!.hgetall(id){ r ->
            result = r.result()
            b = false
            //handleUserInfo.handle(r.result())
        }

        while(b){Thread.sleep(1000)}
        return result
    }

//    fun getAllPersons(): io.vertx.core.json.JsonObject? {
//        var personsHandler = Handler<String> {obj->
//            println(obj)
//        }
//
//        var keyHandler = Handler<JsonArray>{ r->
//            val keys = r.toMutableSet()
//            var resultPersonsArray = JsonArray()
//            val gson = GsonBuilder().setPrettyPrinting().create()
//            for(s: Any in keys){
//                redis!!.hgetall(s.toString()){r->
//                    if(r.succeeded()){
//                        val user = gson.fromJson(r.result().toString(), JsonElement::class.java)
//                        resultPersonsArray.add(user)
//                    }else{
//                        //TODO: ERROR
//                    }
//                }
//            }
//            println("List of persons was getting!")
//        }
//
//        redis!!.keys(keyPattern) {r ->
//            if (r.succeeded()) {
//                keyHandler.handle(r.result())
//
//            }
//        }
//
//        return null
//    }
}
