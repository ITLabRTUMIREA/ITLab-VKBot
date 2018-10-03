package com.rtu.itlab.database

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
        
        config = if (ip != null) {

            if (port != null)

                if (password != null)
                    RedisOptions(host = ip, port = port, auth = password)
                else
                    RedisOptions(host = ip, port = port)
            else

                if (password != null)
                    RedisOptions(host = ip, auth = password)
                else
                    RedisOptions(host = ip)

        } else {

            if (port != null)

                if (password != null)
                    RedisOptions(port = port, auth = password)
                else
                    RedisOptions(port = port)

            else

                if (password != null)
                    RedisOptions(auth = password)
                else
                    RedisOptions()

        }
        redis = RedisClient.create(Vertx.vertx(), config)

        //redis.auth()
    }

}