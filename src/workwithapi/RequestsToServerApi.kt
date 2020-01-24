package workwithapi

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.slf4j.LoggerFactory
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import database.models.UserModelForAuth
import utils.Config
import com.google.gson.JsonObject
import messageprocessing.responses.event.DataView

class RequestsToServerApi {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var accessToken: String? = null

    private var requestCount = 0

    private var apiUrl: String? = null

    private fun updateToken(): Boolean {
        logger.trace("Sending request for update accessToken")
        val clientId = Config().loadPath("apiserver.clientId")
        val secretAuth = Config().loadPath("apiserver.secretAuth")
        val discoveryUri = Config().loadPath("apiserver.discoveryUri")
        val scope = Config().loadPath("apiserver.scope")
        return if (!clientId.isNullOrBlank() &&
            !secretAuth.isNullOrBlank() &&
            !discoveryUri.isNullOrBlank() &&
            !scope.isNullOrBlank()
        ) {
            val (_, _, result) = discoveryUri
                .httpPost()
                .header(
                    "Content-Type" to "application/x-www-form-urlencoded"
                )
                .body("grant_type=client_credentials&scope=$scope&client_id=$clientId&client_secret=$secretAuth")
                .responseObject<JsonObject>()
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException().message
                    logger.error("$ex , Access token not updated")
                    false
                }
                is Result.Success -> {
                    accessToken = result.get().get("access_token").asString
                    logger.trace("Access token updated")
                    true
                }
            }
        } else {
            logger.error("Error reading info from config")
            false
        }
    }

    fun getEventById(eventId: String): DataView? {
        logger.trace("Sending request for getting event by id")
        return if (!accessToken.isNullOrBlank()) {
            apiUrl = Config().loadPath("apiserver.host")
            if (!apiUrl.isNullOrBlank()) {
                val (_, _, result) = "$apiUrl/api/Event/$eventId"
                    .httpGet()
                    .header(
                        "Content-Type" to "application/json",
                        "Authorization" to "Bearer $accessToken"
                    )
                    .responseObject<DataView>()

                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException().message
                        logger.error(ex)
                        if (ex.equals("HTTP Exception 401 Unauthorized")) {
                            if (requestCount == 0) {
                                requestCount++
                                updateToken()
                                val response = getEventById(eventId)
                                requestCount = 0
                                response
                            } else {
                                null
                            }
                        } else
                            null
                    }
                    is Result.Success -> {

                        val response = result.get()
                        logger.info("User (${response.id}) data received")
                        response

                    }
                }
            } else {
                logger.error("Can't load from config path apiserver.host, value is null or blank.")
                null
            }
        } else {
            logger.error("AccessToken is null")
            if (requestCount == 0) {
                requestCount++
                updateToken()
                val result = getEventById(eventId)
                requestCount = 0
                result
            } else {
                null
            }
        }
    }

    fun getUserModelByVkId(vkId: String): User? {
        logger.trace("Sending request for getting user by vkId")
        return if (!accessToken.isNullOrBlank()) {
            apiUrl = Config().loadPath("apiserver.host")
            if (!apiUrl.isNullOrBlank()) {
                val (_, _, result) = "$apiUrl/api/User?vkId=$vkId"
                    .httpGet()
                    .header(
                        "Content-Type" to "application/json",
                        "Authorization" to "Bearer $accessToken"
                    )
                    .responseObject<Array<User>>()

                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException().message
                        logger.error(ex)
                        if (ex.equals("HTTP Exception 401 Unauthorized")) {
                            if (requestCount == 0) {
                                requestCount++
                                updateToken()
                                val response = getUserModelByVkId(vkId)
                                requestCount = 0
                                response
                            } else {
                                null
                            }
                        } else
                            null
                    }
                    is Result.Success -> {
                        if (result.get().isNotEmpty()) {
                            val response = result.get()[0]
                            logger.info("User (${response.id}) data received")
                            response
                        } else {
                            null
                        }
                    }
                }
            } else {
                logger.error("Can't load from config path apiserver.host, value is null or blank.")
                null
            }
        } else {
            logger.error("AccessToken is null")
            if (requestCount == 0) {
                requestCount++
                updateToken()
                val result = getUserModelByVkId(vkId)
                requestCount = 0
                result
            } else {
                null
            }
        }
    }

    fun getUsers(): List<User>? {
        logger.trace("Sending request for getting all users in service")
        return if (!accessToken.isNullOrBlank()) {
            apiUrl = Config().loadPath("apiserver.host")
            if (!apiUrl.isNullOrBlank()) {
                val (_, _, result) = "$apiUrl/api/User?count=100"
                    .httpGet()
                    .header(
                        "Content-Type" to "application/json",
                        "Authorization" to "Bearer $accessToken"
                    )
                    .responseObject<List<User>>()

                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException().message
                        logger.error(ex)
                        if (ex.equals("HTTP Exception 401 Unauthorized")) {
                            if (requestCount == 0) {
                                requestCount++
                                updateToken()
                                val response = getUsers()
                                requestCount = 0
                                response
                            } else {
                                null
                            }
                        } else
                            null
                    }
                    is Result.Success -> {
                        if (result.get().isNotEmpty()) {
                            val response = result.get()
                            logger.info("Users data received")
                            response
                        } else {
                            null
                        }
                    }
                }
            } else {
                logger.error("Can't load from config path apiserver.host, value is null or blank.")
                null
            }
        } else {
            logger.error("AccessToken is null")
            if (requestCount == 0) {
                requestCount++
                updateToken()
                val result = getUsers()
                requestCount = 0
                result
            } else {
                null
            }
        }
    }

    /**
     * Function for getting user id by vk id
     * @param vkId
     * @return user id if found by vk id else null
     */
    fun getIdByVkId(vkId: String): String? {
        val userModel = getUserModelByVkId(vkId)
        return userModel?.id
    }

    /**
     * Function for auth vk account on server
     * @param messageText
     * @param vkId
     * @return userModel if request/response is correct else null
     */
    fun sendTokenToServerForAccess(
        messageText: String,
        vkId: String
    ): User? {
        logger.trace("Sending token for auth in service")
        return if (!accessToken.isNullOrEmpty()) {
            apiUrl = Config().loadPath("apiserver.host")
            if (!apiUrl.isNullOrBlank()) {

                logger.info("Sending a request for authorization of user")
                val (_, _, result) = "$apiUrl/api/account/property/vk"
                    .httpPost()
                    .body(
                        Gson().toJson(UserModelForAuth(messageText.substringAfter("L:"), vkId))
                    )
                    .header(
                        "Content-Type" to "application/json",
                        "Authorization" to "Bearer $accessToken"
                    )
                    .responseString()

                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException().message

                        logger.error(ex)
                        if (ex.equals("HTTP Exception 401 Unauthorized")) {
                            if (requestCount == 0) {
                                requestCount++
                                updateToken()
                                val response = sendTokenToServerForAccess(messageText, vkId)
                                requestCount = 0
                                response
                            } else {
                                null
                            }
                        } else
                            null
                    }
                    is Result.Success -> {
                        val data = result.get()
                        if (data.isNotEmpty()) {
                            val response = Gson().fromJson(data, User::class.java)
                            logger.info("User (${response.id}) data received")
                            response
                        } else {
                            logger.info("User data not received")
                            null
                        }
                    }
                }
            } else {
                logger.error("Can't load from config path apiserver.host, value is null or blank.")
                null
            }
        } else {
            logger.error("AccessToken is null")
            if (requestCount == 0) {
                requestCount++
                updateToken()
                val result = sendTokenToServerForAccess(messageText, vkId)
                requestCount = 0
                result
            } else {
                null
            }

        }
    }
}
