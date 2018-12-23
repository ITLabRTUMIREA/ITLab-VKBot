package com.rtu.itlab.database

import com.google.gson.*
import io.lettuce.core.*
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.api.StatefulRedisConnection
import com.fasterxml.jackson.databind.ObjectMapper
import com.rtu.itlab.utils.mapAnyToMapString
import com.google.gson.JsonObject
import com.rtu.itlab.utils.Config
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import java.time.Duration
import java.util.*
import kotlin.concurrent.timer

/**
 * Time To Live constant
 */
const val TTL = 600000L
const val ANSWERTIMEOUT = 10000L

/**
 * Status code:
 * 1-OK
 * 10-person not found in database
 * 11 - error adding person
 * 12 - error updating person info
 * 13 - some errors while getting persons
 * 14 - some errors while adding persons
 * 15 - cant rewrite (some) person(s)
 * 16 - cant connect to database
 * 17 - error work with database
 * 18 - error connecting to database(reconnecting)
 * 50 - error loading config
 */


class DBClient {

    private val logger: Logger = LoggerFactory.getLogger("com.rtu.itlab.database.DBClient")
    private val userJsonKey: String = "id"
    private val eventJsonKey: String = "id"
    private val userTableKey: String = "users.id:"
    private val eventTableKey: String = "events.id:"

    private val usersKeyPattern = userTableKey + "[0-9a-z]*-[0-9a-z]*-" +
            "[0-9a-z]*-[0-9a-z]*-[0-9a-z]*"

    /**
     * Connecting to redis database server
     * @param password - password uses for auth in redis server database
     * @param ip database server address
     * @param port database port address
     */
    constructor(password: String, ip: String, port: Int) {
        connectToDatabase(password, ip, port)
    }

    /**
     * Connecting to redis database server using info for connection from config
     */
    constructor() {
        loadConfigAndConnect()
    }

    fun loadConfigAndConnect(): JsonObject {
        val config = Config().config
        var jsonResult = JsonObject()
        if (config != null && !config.isEmpty) {
            logger.info("Connecting to database")
            jsonResult = connectToDatabase(
                config.getString("database.password"),
                config.getString("database.url"),
                config.getInt("database.port")
            )
        } else {
            jsonResult.addProperty("statusCode", 50)

        }

        return jsonResult
    }

    private var redisClient: RedisClient? = null
    private var connection: StatefulRedisConnection<String, String>? = null
    private var syncCommands: RedisCommands<String, String>? = null

    /**
     * test Connection to database
     */
    fun isConnected(): Boolean {
        var result = true
        if (syncCommands != null) {
            try {
                syncCommands!!.ping()
            } catch (ex: RedisException) {
                result = false
                logger.error(ex.message)
            }
        }
        return connection != null && connection!!.isOpen && result
    }

    /**
     * Disconnecting from database
     */
    fun closeConnection() {
        if (connection != null) {
            connection!!.close()
        }
        if(timer != null) {
            timer!!.cancel()
            timer!!.purge()
        }
    }

    private var timer: Timer? = null
    private fun timerDatabaseConnection() {
        this.timer = timer("databaseConnectionTimer", initialDelay = TTL, period = 1L) {
            closeConnection()
            logger.info("Connection refused (Timeout TTL = $TTL)")
        }
    }

    private fun connectToDatabase(password: String, ip: String, port: Int): JsonObject {
        val jsonResult = JsonObject()
        try {
            redisClient = RedisClient.create("redis://$password@$ip:$port/0")
            connection = redisClient!!.connect()

            syncCommands = connection!!.sync()
            syncCommands!!.setTimeout(Duration.ofSeconds(ANSWERTIMEOUT))
            logger.info("Connected to database.")
            jsonResult.addProperty("statusCode", 1)
            timerDatabaseConnection()

        } catch (ex: RedisConnectionException) {
            jsonResult.addProperty("statusCode", 16)
            closeConnection()
            logger.error("${ex.message} (Database)")
        }
        return jsonResult
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
        return addPerson(personClass.copy(vkNotice = true, emailNotice = true, phoneNotice = true))
    }

    /**
     * Method for adding person to database
     * @param person DBUser object
     */
    fun addPerson(person: DBUser): JsonObject {
        val userKey = userTableKey + person.id
        val map = ObjectMapper().convertValue(person, HashMap<String, Any?>().javaClass)
        map.remove(userJsonKey)
        return addPerson(userKey, map)
    }


    /**
     * Adding person to database with key: userKey and hashes: map
     * @param userKey - user key in database
     * @param map - hash(user info) in database
     */
    private fun addPerson(userKey: String, map: HashMap<String, Any?>): JsonObject {
        var resultJson = JsonObject()

        if (!isConnected()) loadConfigAndConnect()

        map["phoneNumber"] = map["phoneNumber"].toString().replace(Regex("""[( )_\-]*"""),"")

        if (isConnected()) {
            try {
                if (syncCommands!!.exists(userKey) == 0L) {
                    when (syncCommands!!.hmset(userKey, mapAnyToMapString(map))) {

                        "OK" -> {
                            logger.info("Person ${map["firstName"]} $userKey added!")
                            resultJson.addProperty("statusCode", 1)
                        }

                        else -> {
                            logger.error("Error adding person ${map["firstName"]} $userKey")
                            resultJson.addProperty("statusCode", 11)
                        }

                    }
                } else {
                    logger.warn("Error adding person ${map["firstName"]} $userKey. Cant rewrite person!")
                    resultJson.addProperty("statusCode", 15)
                }

            } catch (ex: io.lettuce.core.RedisCommandExecutionException) {
                logger.error(ex.message)
                resultJson.addProperty("statusCode", 17)
            }

        } else {
            resultJson.addProperty("statusCode", 18)
        }

        return resultJson
    }

    /**
     * Making dump file
     */
    fun makeDump(): JsonObject {
        val jsonResult = JsonObject()

        if (!isConnected()) loadConfigAndConnect()

        if (isConnected()) {
            syncCommands!!.save()
            logger.info("Dump file was created/updated!")
            jsonResult.addProperty("statusCode", 1)
        } else {
            logger.error("Error dump file(Cant connect to database)")
            jsonResult.addProperty("statusCode", 18)
        }
        return jsonResult
    }

    /**
     * Getting Json with info about required person
     * @param key KEY value (id) in database
     * @return Json with person info
     */
    fun getUserInfoByKey(key: String?): JsonObject {
        if (!isConnected()) loadConfigAndConnect()
        val resultJson = JsonObject()

        if (isConnected()) {

            val userKey = when (key!!.startsWith(userTableKey)) {
                true -> key
                else -> userTableKey + key
            }

            val resultMap = when (isConnected()) {
                true -> syncCommands!!.hgetall(userKey)
                else -> mutableMapOf()
            }

            when (resultMap.isNullOrEmpty()) {

                false -> {
                    resultMap[userJsonKey] = userKey.removePrefix(userTableKey)
                    resultJson.add("data", JsonParser().parse(Gson().toJson(resultMap)))
                    logger.info("User information received")
                    resultJson.addProperty("statusCode", 1)
                }

                true -> {
                    logger.error("Can't get user info")
                    resultJson.addProperty("statusCode", 10)
                }

            }
        } else {
            resultJson.addProperty("statusCode", 18)
        }

        return resultJson
    }

    /**
     * Method for adding updated person info to database if person with this id already exist
     * @param person Json with id and new person's info
     * @return result of updating
     */
    fun updatePersonInfo(person: JsonObject): JsonObject {
        val key = person.get(userJsonKey).asString
        val resultJson = JsonObject()

        if (!isConnected()) loadConfigAndConnect()

        if (isConnected()) {
            val userKey = when (key!!.startsWith(userTableKey)) {
                true -> key
                else -> userTableKey + key
            }

            person.remove(userJsonKey)

            when (syncCommands!!.exists(userKey)) {

                0L -> {
                    resultJson.addProperty("statusCode", 12)
                    logger.error("Error updating person info")
                }

                else -> {
                    val mapOfNewValues: Map<String, String> =
                        Gson().fromJson(person, HashMap<String, String>().javaClass)

                    mapOfNewValues.forEach { k, v ->
                        if (syncCommands!!.hget(userKey, k) != null)
                            syncCommands!!.hset(userKey, k, v)
                    }
                    logger.info("Person info is updated!")
                    resultJson.addProperty("statusCode", 1)
                }

            }

        } else {
            resultJson.addProperty("statusCode", 18)
        }

        return resultJson
    }

    /**
     * Deleting all persons from database
     */
    fun deleteAllPersons(): JsonObject {
        if (!isConnected()) loadConfigAndConnect()
        val resultJson = JsonObject()
        if (isConnected()) {
            syncCommands!!.flushall() //TODO : TEST COMMAND
            resultJson.addProperty("statusCode", 1)
            logger.info("All persons deleted")
        } else {
            resultJson.addProperty("statusCode", 18)
        }
        return resultJson
    }

    /**
     * Getting Json with info about all persons in database
     * @return Json with persons info
     */
    fun getAllPersons(): JsonObject {
        if (!isConnected()) loadConfigAndConnect()

        var wasErrors = false
        val resultJson = JsonObject()
        if (isConnected()) {
            var keys: List<String> = mutableListOf()
            try {
                keys = syncCommands!!.keys(usersKeyPattern)
            } catch (ex: RedisException) {
                wasErrors = true
                logger.error(ex.message + " REDIS ERROR")
            } catch (e: NullPointerException) {
                wasErrors = true
                logger.error(e.message + " NULL POINTER EXCEPTION")
            }

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
                logger.info("Persons got with some errors")
                resultJson.addProperty("statusCode", 13)
            } else {
                logger.info("Persons got!")
                resultJson.addProperty("statusCode", 1)
            }
        } else {
            resultJson.addProperty("statusCode", 18)
        }
        return resultJson
    }

    /**
     * Deleting person from database by id(key)
     * @param id person id
     */
    fun deletePerson(id: String?): JsonObject {
        if (!isConnected()) loadConfigAndConnect()
        val resultJson = JsonObject()
        if (isConnected()) {
            val userKey = when (id!!.startsWith(userTableKey)) {
                true -> id
                else -> userTableKey + id
            }


            when (syncCommands!!.del(userKey)) {
                0L -> {
                    resultJson.addProperty("statusCode", 10)
                    logger.info("Person not deleted!")
                }
                else -> {
                    resultJson.addProperty("statusCode", 1)
                    logger.info("Person deleted!")
                }
            }
        } else {
            resultJson.addProperty("statusCode", 18)
        }

        return resultJson
    }

    /**
     * Get a set of users mails who want to receive notifications by mail.
     * @return set of emails
     */
    fun getUsersMailsForEmailMailing(): JsonObject {
        val jsonResult = JsonObject()
        val result = mutableSetOf<String>()

        if (!isConnected()) loadConfigAndConnect()

        if (isConnected()) {

            var keys = mutableListOf<String>()

            jsonResult.addProperty("statusCode", 1)
            keys = syncCommands!!.keys(usersKeyPattern)

            keys.forEach { key ->
                val email = syncCommands!!.hget(key, "email")
                val emailNotice = syncCommands!!.hget(key, "emailNotice")!!.toBoolean()

                if (emailNotice && !email.isNullOrBlank())
                    result.add(email)
            }

        } else {
            jsonResult.addProperty("statusCode", 18)
        }

        val element = Gson().toJsonTree(result, object : TypeToken<MutableSet<String>>() {}.type)
        jsonResult.add("emails", element.asJsonArray)

        return jsonResult
    }

    /**
     * Get a set of users vk ids who want to receive notifications by vk.
     * @return result jsonObject
     */
    fun getUsersVkIdForVkMailing(): JsonObject {
        val jsonResult = JsonObject()
        val result = mutableSetOf<Int>()
        if (!isConnected()) loadConfigAndConnect()

        if (isConnected()) {

            var keys: List<String> = mutableListOf()
            jsonResult.addProperty("statusCode", 1)
            keys = when (isConnected()) {
                true -> syncCommands!!.keys(usersKeyPattern)
                else -> mutableListOf()
            }


            keys.forEach { key ->
                val vkId = syncCommands!!.hget(key, "vkId")
                val vkNotice = syncCommands!!.hget(key, "vkNotice")!!.toBoolean()

                if (vkNotice && !vkId.isNullOrBlank())
                    result.add(vkId.toInt())

            }
        } else {
            jsonResult.addProperty("statusCode", 18)
        }

        val element = Gson().toJsonTree(result, object : TypeToken<MutableSet<Int>>() {}.type)
        jsonResult.add("vkIDs", element.asJsonArray)

        return jsonResult
    }

    /**
     * Get a set of users vk ids who want to receive notifications by vk.
     * @param invitedUsers List of invited Users
     * @return result jsonObject
     */
    fun getUsersVkIdForVkMailing(invitedUsers: List<DBUser>): JsonObject {
        val jsonResult = JsonObject()
        val result = mutableSetOf<Int>()
        if (!isConnected()) loadConfigAndConnect()

        if (isConnected()) {
            jsonResult.addProperty("statusCode", 1)
            invitedUsers.forEach { dbUser ->
                when (val vkId = syncCommands!!.hget(userTableKey + dbUser.id, "vkId")) {
                    null -> logger.error("User ${dbUser.firstName} ${dbUser.lastName} was not found in database")
                    else -> {
                        if (vkId.toInt() != 0)
                            result.add(vkId.toInt())
                    }
                }
            }

        } else {
            jsonResult.addProperty("statusCode", 18)
        }

        val element = Gson().toJsonTree(result, object : TypeToken<MutableSet<Int>>() {}.type)
        jsonResult.add("vkIDs", element.asJsonArray)

        return jsonResult
    }


    /**
     * Get a set of users phones numbers who want to receive notifications by phone.
     * @return result jsonObject
     */
    fun getUsersPhonesForPhoneMailing(): JsonObject {
        val jsonResult = JsonObject()
        val result = mutableSetOf<String>()
        var keys: List<String> = mutableListOf()
        if (!isConnected()) loadConfigAndConnect()

        if (isConnected()) {
            keys = syncCommands!!.keys(usersKeyPattern)
            jsonResult.addProperty("statusCode", 1)

            keys.forEach { key ->
                val phoneNumber = syncCommands!!.hget(key, "phoneNumber")
                val phoneNotice = syncCommands!!.hget(key, "phoneNotice")!!.toBoolean()

                if (phoneNotice && !phoneNumber.isNullOrBlank())
                    result.add(phoneNumber)
            }

        } else {
            jsonResult.addProperty("statusCode", 18)
        }

        val element = Gson().toJsonTree(result, object : TypeToken<MutableSet<String>>() {}.type)
        jsonResult.add("phones", element.asJsonArray)

        return jsonResult
    }

    /**
     * Method for check user for availability in database
     * @param vkId user vk id
     */
    fun isUserInDBByVkId(vkId: Int): JsonObject {
        //TODO: OPTIMIZE CODE

        val jsonResult = JsonObject()
        if (!isConnected()) loadConfigAndConnect()
        if (isConnected()) {
            var result = false
            val keys = syncCommands!!.keys(usersKeyPattern)
            for (key in keys) {
                val userVkId = syncCommands!!.hget(key, "vkId")
                if (vkId == userVkId.toInt()) {
                    jsonResult.addProperty("id", key.removePrefix(userTableKey))
                    result = true
                    break
                }
            }
            jsonResult.addProperty("statusCode", 1)
            jsonResult.addProperty("result", result)
        } else {
            jsonResult.addProperty("statusCode", 18)
        }

        return jsonResult
    }

    /**
     * Adding array of persons to database
     * @param persons array of persons
     */
    fun addPersons(persons: JsonArray): JsonObject {
        return addPersons(Gson().fromJson(persons, Array<DBUser>::class.java))
    }

    /**
     * Adding array of persons to database
     * @param persons array of DBUsers
     */
    fun addPersons(persons: Array<DBUser>): JsonObject {
        var wasErrors = false
        var alreadyExists = false
        val jsonResult = JsonObject()

        persons.forEach {
            when (addPerson(it.copy(vkNotice = true, emailNotice = true, phoneNotice = true)).get("statusCode").asInt) {
                11, 18, 16 -> {
                    wasErrors = true
                }
                15 -> {
                    alreadyExists = true
                }
            }
        }

        if (wasErrors) {
            jsonResult.addProperty("statusCode", 14)
            logger.info("Was some errors or warnings while adding persons")
        } else if (alreadyExists) {
            jsonResult.addProperty("statusCode", 15)
            logger.info("Some persons already exists in database. Cant rewrite")
        } else {
            jsonResult.addProperty("statusCode", 1)
            logger.info("Persons added")
        }

        return jsonResult
    }

}

