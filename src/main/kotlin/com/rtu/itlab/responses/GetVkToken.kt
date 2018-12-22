package com.rtu.itlab.responses

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.utils.ServerResponseJson
import com.rtu.itlab.utils.UserCard

class GetVkToken(tmp: JsonObject?, db: DBClient) : ResponseHandler(db) {

    private val vkId = tmp!!.getAsJsonObject("object").get("from_id").asInt
    private val token: String = tmp!!.getAsJsonObject("object").get("text").asString

    fun sendMessage(message: String) {
        vk.messages()
            .send(actor)
            .userId(vkId)
            .message(message)
            .execute()
    }

    fun sendDefaultKeyboard() {
        //TODO:SEND KEYBOARD TO USER

    }

    override fun send(): JsonObject {
        var message = "Nothing..."
        if (!db!!.isUserInDBByVkId(vkId).get("result").asBoolean) {
            if (token.startsWith("L:")) {
                Fuel.post(config.getString("apiserver.host") + "/api/account/property/vk")
                    .body(Gson().toJson(UserCard(token.substringAfter("L:"), vkId)))
                    .header(
                        "Content-Type" to "application/json",
                        "Authorization" to config.getString("apiserver.accessToken")
                    )
                    .responseObject<ServerResponseJson> { _, _, result ->
                        when {
                            result.get().statusCode == 1 -> {

                                //Getting result of adding person to database
                                val addingResult = db!!.addPerson(
                                    result.get().data.copy(
                                        vkId = vkId.toString(), vkNotice = true,
                                        emailNotice = true, phoneNotice = true
                                    )
                                ).get("statusCode").asInt


                                //If person added then 1, else send message to user with error 1
                                message = when (addingResult) {
                                    1 -> "Поздравляем, ваша учетня запись прикреплена"
                                    else -> "При добавлении вашей учетной записи произошла ошибка 1"
                                }

                            }

                            //If the auth code (token) was entered incorrectly
                            result.get().statusCode == 26 -> message = "Проверьте правильность написания кода"

                            //If there are any other errors
                            else -> message = "При добавлении вашей учетной записи произошла ошибка 2"
                        }
                    }
            } else {
                //if the user has sent a non-template code message
                message = "Если вы пытались прислать код для верификация, то вы сделали это как-то не так\n" +
                        "Я не веду бесед с незнакомцами\n" +
                        "Проверьте правильность написания кода"
            }
        } else {
            //If user already authorized in the system
            if (token.startsWith("L:"))
                message = "Ранее вы уже были авторизованы!"
            else
                message = "Я не понимаю, что вы хотели мне сказать."

            //TODO: DECLINE EMAIL VK PHONE
        }

        sendMessage(message)

        return resultJson
    }
}
