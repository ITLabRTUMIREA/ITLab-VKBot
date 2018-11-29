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

    override fun send() :JsonObject{
        if(!db!!.isUserInDBByVkId(vkId)) {
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
                                val addingResult = db!!.addPerson(
                                    result.get().data.copy(
                                        vkId = vkId.toString(), vkNotice = true,
                                        emailNotice = true, phoneNotice = true
                                    )
                                ).get("statusCode").asInt
                                if (addingResult == 1) {
                                    vk.messages()
                                        .send(actor)
                                        .userId(vkId)
                                        .message("Поздравляем, ваша учетня запись прикреплена")
                                        .execute()
                                } else {
                                    vk.messages()
                                        .send(actor)
                                        .userId(vkId)
                                        .message("Что-то пошло не так")
                                        .execute()
                                }

                            }
                            result.get().statusCode == 26 -> vk.messages()
                                .send(actor)
                                .userId(vkId)
                                .message("Проверьте правильность написания кода")
                                .execute()
                            else -> vk.messages()
                                .send(actor)
                                .userId(vkId)
                                .message("Что-то пошло не так")
                                .execute()
                        }
                    }
            } else {
                vk.messages()
                    .send(actor)
                    .userId(vkId)
                    .message(
                        "Если вы пытались прислать код для верификация, то вы сделали это как-то не так\n" +
                                "Я не веду бесед с незнакомцами\n" +
                                "Проверьте правильность написания кода"
                    )
                    .execute()
            }
        }else{
            //TODO: DECLINE EMAIL VK PHONE
        }
        return resultJson
    }
}
