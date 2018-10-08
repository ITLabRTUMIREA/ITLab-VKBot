package com.rtu.itlab.database

import com.google.gson.JsonObject
import io.vertx.core.Vertx
import io.vertx.kotlin.redis.RedisOptions
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions


//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.SchemaUtils.create
//import org.jetbrains.exposed.sql.Table
//import org.jetbrains.exposed.sql.transactions.transaction
//
//object Users : Table() {
//    val id = integer("id").autoIncrement().primaryKey()
//    val serverId = varchar("serverId", 40)
//    val firstName = varchar("firstName", 15)
//    val lastName = varchar("lastName", 15)
//    val email = varchar("email", 25).nullable()
//    val phoneNumber = varchar("phoneNumber", 25).nullable()
//    val vkId = (integer("vkId"))
//}
//
//object Notice : Table() {
//    val id = integer("id").autoIncrement().primaryKey()
//    val vkNotice = bool("vkNotice")
//    val phoneNotice = bool("phoneNotice")
//}
//
//fun main(args: Array<String>) {
//    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
//
//    transaction {
//        create(Users, Notice)
//    }
//}

class DB {

    private var config: RedisOptions? = null
    private var redis: RedisClient? = null
    private val keyPattern = "[1-9a-z]{8}-[1-9a-z]{4}-" +
            "[1-9a-z]{4}-[1-9a-z]{4}-[1-9a-z]{12}"

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
//        config = if (ip != null) {
//
//            if (port != null)
//
//                if (password != null)
//                    RedisOptions(host = ip, port = port, auth = password)
//                else
//                    RedisOptions(host = ip, port = port)
//            else
//
//                if (password != null)
//                    RedisOptions(host = ip, auth = password)
//                else
//                    RedisOptions(host = ip)
//
//        } else {
//
//            if (port != null)
//
//                if (password != null)
//                    RedisOptions(port = port, auth = password)
//                else
//                    RedisOptions(port = port)
//
//            else
//
//                if (password != null)
//                    RedisOptions(auth = password)
//                else
//                    RedisOptions()
//
//        }
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
        val personInfo = io.vertx.core.json.JsonObject(person.asString)
        val id = personInfo.getString("id")
        personInfo.remove("id")
        redis!!.hmset(id, personInfo) { r ->
            if (r.succeeded()) {
                print("Person added!")
            }
        }
    }

    fun updatePesonValue(personId: String, key: String, value: String) {
        redis!!.hset(personId, key, value) { r ->
            if (r.succeeded()) {
                print("Person info is updated!")
            }
        }
    }

    fun getAllPersons():JsonObject?{
        var list = redis!!.keys(keyPattern){r ->
            if(r.succeeded()){
                print("List of keys was getting")
            }
        }
        print(list.toString())
        return null
    }


}

fun main(args: Array<String>) {
    val db = DB("1230", null, null)
    //db.addPerson()
//    val redisClientVerticle = RedisClientVerticle()
//    redisClientVerticle.start()
}

//class RedisClientVerticle : io.vertx.core.AbstractVerticle() {
//    override fun start() {
//        // If a config file is set, read the host and port.
//        var host = "127.0.0.1"
//        if (host == null) {
//            host = "127.0.0.1"
//        }
//
//        // Create the redis client
//        var client = RedisClient.create(Vertx.vertx(), RedisOptions(
//                host = host, auth = "1230"))
//
//        client.set("key", "value", { r ->
//            if (r.succeeded()) {
//                println("key stored")
//                client.get("key", { s ->
//                    println("Retrieved value: ${s.result()}")
//                })
//            } else {
//                println("Connection or Operation Failed ${r.cause()}")
//            }
//        })
//    }
//}