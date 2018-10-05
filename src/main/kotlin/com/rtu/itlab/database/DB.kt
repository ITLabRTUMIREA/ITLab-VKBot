package com.rtu.itlab.database

import com.google.gson.JsonObject
import com.rtu.itlab.main
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.redis.RedisOptions
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import io.vertx.core.VertxOptions


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

        redis!!.set("key", "value") { r ->
            if (r.succeeded()) {
                println("key stored")
                redis!!.get("key") { s ->
                    println("Retrieved value: ${s.result()}")
                }
            } else {
                println("Connection or Operation Failed ${r.cause()}")
            }
        }

    }

    fun addPerson(tableName: String, values: JsonObject) {
        var value = io.vertx.core.json.JsonObject(values.asString)

        redis!!.hmset(tableName, value) { r ->
            if (r.succeeded()) {
                print("Person added to table $tableName")
            }
        }
    }
}

fun main(args: Array<String>) {
    val db = DB("1230", null, null)
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