package com.rtu.itlab.database

import com.google.gson.*
import io.lettuce.core.*
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.api.StatefulRedisConnection
import java.util.HashMap
import com.fasterxml.jackson.databind.ObjectMapper
import com.rtu.itlab.utils.mapAnyToMapString
import com.google.gson.JsonObject




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
        } catch (ex: io.lettuce.core.RedisConnectionException) {
            println(ex.message)
        }

    }

    /**
     * Method for adding person info
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
        val dbUser = Gson().fromJson(person.toString(), DBUser::class.java)
        dbUser.id = id

        if (!person.has("vkNotice")) dbUser.vkNotice = true
        if (!person.has("emailNotice")) dbUser.emailNotice = true
        if (!person.has("phoneNotice")) dbUser.phoneNotice = true

        val map = ObjectMapper().convertValue(dbUser, HashMap<String, String>().javaClass)
        syncCommands!!.hmset(id, mapAnyToMapString(map))
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
        resultMap.put(KEY, person.get(KEY).asString)
        val resultJson = Gson().toJson(resultMap)

        return JsonParser().parse(resultJson).asJsonObject
    }

    /**
     * Method for adding person info to database if person with this id already exist
     * @param person Json with id and new person's info
     * @return result of updating
     */
    fun updatePersonInfo(person: JsonObject): String {
        val id = person.get(KEY).asString
        person.remove(KEY)
        if (!syncCommands!!.hgetall(id).isEmpty()) {
            var map: Map<String, String> = HashMap()
            map = Gson().fromJson(person, map.javaClass)
            for (element in map) {
                if (syncCommands!!.hget(id, element.key) != null)
                    syncCommands!!.hset(id, element.key, element.value)

            }
            return "OK"
        } else {
            return "Not Found"
        }
    }

    /**
     * Deleting all persons from database
     */
    fun deleteAllPersons() {
        val keys = syncCommands!!.keys(keyPattern)
        for (key in keys) {
            val jsonObject = JsonObject()
            jsonObject.addProperty(KEY, key)
            deletePerson(jsonObject)
        }
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
        result.add("data", persons)
        return result
    }

    /**
     * Deleting person from redis database
     * @param person Json with information about the person you want to delete
     * that should contain person KEY value
     */
    fun deletePerson(person: JsonObject) {
        val id = person.get(KEY).asString
        syncCommands!!.del(id)
        println("User deleted!")
        makeDump()
    }

    /**
     * Getting set of users email which will be sent an email alert
     * @return set of emails
     */
    fun getUsersMailsForEmailMailing(): Set<String> {
        val result = mutableSetOf<String>()
        val keys = syncCommands!!.keys(keyPattern)
        for (key in keys) {
            var email = syncCommands!!.hget(key, "email")
            if (syncCommands!!.hget(key, "emailNotice").toBoolean() &&
                    (!email.equals("")) && (email != null) && (!email.equals("null")))
                result.add(email)
        }
        return result
    }

    /**
     * Getting set of users vkIds which will be sent an vk alert
     * @return set of VkIds
     */
    fun getUsersVkIdForVkMailing(): Set<String> {
        val result = mutableSetOf<String>()
        val keys = syncCommands!!.keys(keyPattern)
        for (key in keys) {
            var vkId = syncCommands!!.hget(key, "vkId")
            if (syncCommands!!.hget(key, "vkNotice").toBoolean() &&
                    (!vkId.equals("")) && (vkId != null) && (!vkId.equals("null")))
                result.add(vkId)
        }
        return result
    }

    /**
     * Getting set of users phones which will be sent an phone alert
     * @return set of phones
     */
    fun getUsersPhonesForPhoneMailing(): Set<String> {
        val result = mutableSetOf<String>()
        val keys = syncCommands!!.keys(keyPattern)
        for (key in keys) {
            var phoneNumber = syncCommands!!.hget(key, "phoneNumber")
            if (syncCommands!!.hget(key, "phoneNotice").toBoolean() &&
                    (!phoneNumber.equals("")) && (phoneNumber != null) && (!phoneNumber.equals("null")))
                result.add(phoneNumber)
        }
        return result
    }

    /**
     * Adding array of persons to database
     * @param persons contains {"data":[....]}
     */
    fun addPersons(persons: JsonObject) {
        val personsArray = persons.get("data").asJsonArray
        for(person in personsArray){
            addPerson(person.asJsonObject)
        }
    }
}

