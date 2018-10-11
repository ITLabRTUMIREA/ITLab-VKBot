package com.rtu.itlab.database

import com.google.gson.*
import io.lettuce.core.*
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.api.StatefulRedisConnection
import java.util.HashMap


class DBClient {

    private val keyPattern = "[0-9a-z]*-[0-9a-z]*-" +
            "[0-9a-z]*-[0-9a-z]*-[0-9a-z]*"

    private val KEY: String = "id"

    private var redisClient: RedisClient? = null
    private var connection: StatefulRedisConnection<String, String>? = null
    private var syncCommands: RedisCommands<String, String>? = null

    /**
     * Default constructor
     */
    constructor() {}

    /**
     * Connecting to redis database server
     * @param password - password uses for auth in redis server database
     * @param ip pc server address
     * @param port pc port address
     */
    constructor(password: String = "", ip: String = "127.0.0.1", port: Int = 6379) {
        createConnectionToDB(password, ip, port)
    }

    /**
     * Connecting to redis database server
     *
     * @param password - password uses for auth in redis server database
     * @param ip pc server address
     * @param port pc port address
     */
    fun createConnectionToDB(password: String = "", ip: String = "127.0.0.1", port: Int = 6379) {

        try {
            redisClient = RedisClient.create("redis://$password@$ip:$port/0")
            connection = redisClient!!.connect()
            syncCommands = connection!!.sync()
            println("Connected to redis database")
        } catch (ex: io.lettuce.core.RedisConnectionException){
            println(ex.message)
        }

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
     * @param person Json that should contain person info, example above
     */
    fun addPerson(person: JsonObject) {
        val id = person.get(KEY).asString
        person.remove(KEY)
        val gson = Gson()
        var map: Map<String, String> = HashMap()
        map = gson.fromJson(person.toString(), map.javaClass) as Map<String, String>
        syncCommands!!.hmset(id, map)
        println("Person added!")
        makeDump()
    }

    /**
     * Making dump file
     */
    fun makeDump() {
        syncCommands!!.save()
        println("Dump file was created/updated!")
    }

    /**
     * Getting Json with info about required person
     * @param person Json that should contain person KEY value
     * @return Json with person info
     */
    fun getUserInfoByKey(person: JsonObject): JsonObject {

        val resultMap = syncCommands!!.hgetall(person.get(KEY).asString)

        val resultJson = Gson().toJson(resultMap)

        return JsonParser().parse(resultJson).asJsonObject
    }

    /**
     * Getting Json with info about all persons in redis database
     * @return Json with persons info
     */
    fun getAllPersons(): JsonObject? {

        var result = JsonObject()
        val keys = syncCommands!!.keys(keyPattern)
        var persons = JsonArray()
        for (key in keys) {
            var person = JsonObject()
            var personKeysValues = syncCommands!!.hgetall(key)
            for (personKeyValue in personKeysValues) {
                person.addProperty(personKeyValue.key, personKeyValue.value)
            }
            persons.add(person)
        }
        result.add("Users", persons)
        return result
    }
    /**
     * Deleting person from redis database
     * @param person Json with information about the person you want to delete
     * that should contain person KEY value
     */
    fun deletePerson(person: JsonObject){
        val id = person.get(KEY).asString
        syncCommands!!.del(id)
        println("User deleted!")
        makeDump()
    }
}

