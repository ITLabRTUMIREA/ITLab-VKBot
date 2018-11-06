package com.rtu.itlab.database

import com.google.gson.*
import io.lettuce.core.*
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.api.StatefulRedisConnection
import java.util.HashMap
import com.fasterxml.jackson.databind.ObjectMapper
import com.rtu.itlab.utils.mapAnyToMapString
import com.google.gson.JsonObject

/**
 * Status code:
 * 1-OK
 * 10-person not found in database
 * 11 - error adding person
 * 12 - error updating person info
 * 13 - some errors while getting persons
 * 14 - some errors while adding persons
 */

/**
 * Connecting to redis database server
 * @param password - password uses for auth in redis server database
 * @param ip database server address
 * @param port database port address
 */
class DBClient(password: String = "", ip: String = "127.0.0.1", port: Int = 6379) {

    private val userJsonKey: String = "id"
    private val eventJsonKey: String = "id"
    private val userTableKey: String = "users.id:"
    private val eventTableKey: String = "events.id:"

    private val usersKeyPattern = userTableKey + "[0-9a-z]*-[0-9a-z]*-" +
            "[0-9a-z]*-[0-9a-z]*-[0-9a-z]*"

    private var redisClient: RedisClient? = null
    private var connection: StatefulRedisConnection<String, String>? = null
    private var syncCommands: RedisCommands<String, String>? = null

    init {
        try {
            redisClient = RedisClient.create("redis://$password@$ip:$port/0")
            connection = redisClient!!.connect()
            syncCommands = connection!!.sync()
            println("Connected to redis database")
        } catch (ex: RedisConnectionException) {
            println(ex.message)
        }
    }

    /**
     * Method for adding person info to database
     * vkNotice,emailNotice,phoneNotice - boolean values
     * id,firstName,lastName,phoneNumber,email,vkId - string values
     * example of JsonObject
     * {
    "id": "1111-1111-1111-1111-1111",
    "firstName": "Дмитрий",
    "lastName": "Романов",
    "phoneNumber": "+71111111111",
    "email": "qwe@gmail.com",
    "vkId": "1111111",
    "vkNotice": "true",
    "emailNotice": "true",
    "phoneNotice": "true"
     * }
     * @param person Json that should contain person info, example above
     */
    fun addPerson(person: JsonObject): JsonObject {
        val personClass = Gson().fromJson(person.toString(), DBUser::class.java)
        val map = ObjectMapper().convertValue(personClass.copy(vkNotice = true,
                emailNotice = true, phoneNotice = true), HashMap<String, String>().javaClass)
        map.remove(userJsonKey)
        val userKey = userTableKey + personClass.id

        return addPerson(userKey, map)
    }

    /**
     * Method for adding person to database
     * @param person DBUser object
     */
    fun addPerson(person: DBUser): JsonObject {
        val userKey = userTableKey + person.id
        val map = ObjectMapper().convertValue(person, HashMap<String, String>().javaClass)
        map.remove(userJsonKey)
        return addPerson(userKey, map)
    }

    /**
     * Adding person to database with key: userKey and hashes: map
     * @param userKey - user key in database
     * @param map - hash(user info) in database
     */
    private fun addPerson(userKey: String, map: HashMap<String, String>): JsonObject {
        val resultJson = JsonObject()
        when (syncCommands!!.hmset(userKey, mapAnyToMapString(map))) {

            "OK" -> {
                println("Person added!")
                makeDump()
                resultJson.addProperty("statusCode", 1)
            }

            else -> {
                println("Error adding person")
                resultJson.addProperty("statusCode", 11)
            }

        }

        return resultJson
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
     * @param key KEY value (id) in database
     * @return Json with person info
     */
    fun getUserInfoByKey(key: String?): JsonObject {
        val userKey = userTableKey + key

        val resultMap = syncCommands!!.hgetall(userKey)
        val resultJson = JsonObject()

        when (resultMap.isNullOrEmpty()) {

            false -> {
                resultMap[userJsonKey] = key //TODO: test kotlin feature 1
                resultJson.add("data", JsonParser().parse(Gson().toJson(resultMap)))
                println("User information received")
                resultJson.addProperty("statusCode", 1)
            }

            true -> {
                println("Can't get user info")
                resultJson.addProperty("statusCode", 10)
            }

        }

        //resultMap.put(KEY, person.get(KEY).asString) //old working version

        return resultJson
    }

    /**
     * Method for adding updated person info to database if person with this id already exist
     * @param person Json with id and new person's info
     * @return result of updating
     */
    fun updatePersonInfo(person: JsonObject): JsonObject {

        val id = person.get(userJsonKey).asString
        person.remove(userJsonKey)
        val userKey = userTableKey + id
        val resultJson = JsonObject()
        //return when (syncCommands!!.hgetall(userKey).isNullOrEmpty()) {
        when (syncCommands!!.exists(userKey)) { //TODO : TEST UPDATE

            0L -> {
                resultJson.addProperty("statusCode", 12)
                println("Error updating person info")
            }

            else -> {
                val mapOfNewValues: Map<String, String> = Gson().fromJson(person, HashMap<String, String>().javaClass)

                mapOfNewValues.forEach { key, value ->
                    if (!syncCommands!!.hget(userKey, key).isNullOrEmpty())
                        syncCommands!!.hset(userKey, key, value)
                }
                resultJson.addProperty("statusCode", 1)
            }

        }

        return resultJson
    }

    /**
     * Deleting all persons from database
     */
    fun deleteAllPersons(): JsonObject {
        syncCommands!!.flushall() //TODO : TEST COMMAND
        val resultJson = JsonObject()
        resultJson.addProperty("statusCode", 1)
//        val keys = syncCommands!!.keys(keyPattern)
//        for (key in keys) {
//            val jsonObject = JsonObject()
//            jsonObject.addProperty(KEY, key)
//            deletePerson(jsonObject)
//        }
        return resultJson
    }

    /**
     * Getting Json with info about all persons in database
     * @return Json with persons info
     */
    fun getAllPersons(): JsonObject {
        val resultJson = JsonObject()

        val keys = syncCommands!!.keys(usersKeyPattern)

        var wasErrors = false

        val persons = JsonArray()

        keys.forEach { key ->
            val userInfo = getUserInfoByKey(key)

            when (userInfo.get("statusCode").asInt) {
                1 -> {
                    persons.add(userInfo.get("data"))
                }
                else -> {
                    wasErrors = true
                }
            }

        }

        resultJson.add("data", persons)

        if (wasErrors) {
            println("Persons got with some errors")
            resultJson.addProperty("statusCode", 13)
        } else {
            println("Persons got!")
            resultJson.addProperty("statusCode", 1)
        }
        return resultJson
    }

    /**
     * Deleting person from database by id(key)
     * @param id person id
     */
    fun deletePerson(id: String?): JsonObject {
        val jsonResult = JsonObject()
        when (syncCommands!!.del(id)) {
            0L -> {
                jsonResult.addProperty("statusCode", 10)
                println("Person not deleted!")
            }
            else -> {
                jsonResult.addProperty("statusCode", 1)
                println("Person deleted!")
                makeDump()
            }
        }

        return jsonResult
    }

    /**
     * Get a set of users mails who want to receive notifications by mail.
     * @return set of emails
     */
    fun getUsersMailsForEmailMailing(): Set<String> {
        val result = mutableSetOf<String>()
        val keys = syncCommands!!.keys(usersKeyPattern)
        keys.forEach { key ->
            val email = syncCommands!!.hget(key, "email")
            val emailNotice = syncCommands!!.hget(key, "emailNotice")!!.toBoolean()

            if (emailNotice && !email.isNullOrBlank())
                result.add(email)

        }
        return result
    }

    /**
     * Get a set of users vk ids who want to receive notifications by vk.
     * @return set of VkIds
     */
    fun getUsersVkIdForVkMailing(): Set<Int> {
        val result = mutableSetOf<Int>()
        val keys = syncCommands!!.keys(usersKeyPattern)

        keys.forEach { key ->
            val vkId = syncCommands!!.hget(key, "vkId")
            val vkNotice = syncCommands!!.hget(key, "vkNotice")!!.toBoolean()

            if (vkNotice && !vkId.isNullOrBlank())
                result.add(vkId.toInt())

        }

        return result
    }

    /**
     * Get a set of users phones numbers who want to receive notifications by phone.
     * @return set of phones
     */
    fun getUsersPhonesForPhoneMailing(): Set<String> {
        val result = mutableSetOf<String>()
        val keys = syncCommands!!.keys(usersKeyPattern)

        keys.forEach { key ->
            val phoneNumber = syncCommands!!.hget(key, "phoneNumber")
            val phoneNotice = syncCommands!!.hget(key, "phoneNotice")!!.toBoolean()

            if (phoneNotice && !phoneNumber.isNullOrBlank())
                result.add(phoneNumber)

        }

        return result
    }

    /**
     * Adding array of persons to database
     * @param persons array of persons
     */
    fun addPersons(persons: JsonArray): JsonObject {
        var wasErrors = false
        val jsonResult = JsonObject()

        persons.forEach {
            when (addPerson(it.asJsonObject).get("statusCode").asInt) {
                11 -> {
                    wasErrors = true
                }
            }
        }

        if (wasErrors) {
            jsonResult.addProperty("statusCode", 14)
            println("Was some errors while adding persons")
        } else {
            jsonResult.addProperty("statusCode", 1)
            println("Persons added")
        }

        return jsonResult
    }

    /**
     * Adding array of persons to database
     * @param persons array of DBUsers
     */
    fun addPersons(persons: Array<DBUser>): JsonObject {
        var wasErrors = false
        val jsonResult = JsonObject()

        persons.forEach {
            when (addPerson(it).get("statusCode").asInt) {
                11 -> {
                    wasErrors = true
                }
            }
        }

        if (wasErrors) {
            jsonResult.addProperty("statusCode", 14)
            println("Was some errors while adding persons")
        } else {
            jsonResult.addProperty("statusCode", 1)
            println("Persons added")
        }

        return jsonResult
    }
}

