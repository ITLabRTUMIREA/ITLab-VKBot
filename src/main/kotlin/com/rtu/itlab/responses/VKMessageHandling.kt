package com.rtu.itlab.responses

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.bot.BotCommands
import com.rtu.itlab.bot.keyboard.getKeyboardForCurrentPerson
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.utils.ServerResponseJson
import com.rtu.itlab.utils.UserCard
import com.rtu.itlab.bot.keyboard.keyboard
import com.rtu.itlab.emailsender.*

class VKMessageHandling(tmp: JsonObject?, db: DBClient) : ResponseHandler(db) {

    override fun sendEmail() {
        val html = HtmlEmail()
        html.changeDescription(
            "<p>Ваш аккаунт сайта ${config.getString("frontend.host")} был привязан к системе уведомлений</p>" +
                    "<p>Управлять(отключать или включать) уведомлениями вы можете путем общения с ботом RTU IT Lab</p>"
        )

        html.changeTitle("Уведомление об успешном подключении системы уведомлений")

        html.changeUrl(config.getString("frontend.host"))

        val userEmail = db!!.getDbUserByKey(db.isUserInDBByVkId(vkId).get("id").asString)!!.email!!

        sendMail(
            UserMail(config.getString("mail.email"), config.getString("mail.password")),
            MailMessage("RTUITLAB NOTIFICATION", html.getHtmlString().replace("Перейти к событию", "Перейти на сайт")),
            HostMail(config.getString("mail.port"), config.getString("mail.host")),
            setOf(userEmail)
        )
    }

    private val vkId = tmp!!.getAsJsonObject("object").get("from_id").asInt
    private val messageText: String = tmp!!.getAsJsonObject("object").get("text").asString
    private var keyboard = "{\"buttons\":[],\"one_time\":true}"

    private fun sendMessage(message: String) {
        vk.messages()
            .send(actor)
            .userId(vkId)
            .message(message)
            .keyboard(keyboard)
            .execute()
    }

    private fun getKeyboardJson(): String {
        var result = ""
        val keyboardClass = getKeyboardForCurrentPerson(vkId, db!!)
        if (keyboardClass.lines.size > 0)
            result = keyboardClass.getKeyboardJson().toString()
        return result
    }

    override fun send(): JsonObject {
        var message: String


        if (db!!.isUserInDBByVkId(vkId).get("result").asString != "true") {

            if (messageText.startsWith("L:")) {

                val (_, _, result) = Fuel.post(config.getString("apiserver.host") + "/api/account/property/vk").body(
                    Gson().toJson(UserCard(messageText.substringAfter("L:"), vkId))
                ).header(
                    "Content-Type" to "application/json",
                    "Authorization" to config.getString("apiserver.accessToken")
                ).responseObject<ServerResponseJson>()

                message = when (result.get().statusCode) {
                    1 -> {
                        //Getting result of adding person to database
                        val addingResult = db.addPerson(
                            result.get().data.copy(
                                vkId = vkId.toString(), vkNotice = true,
                                emailNotice = true, phoneNotice = true
                            )
                        ).get("statusCode").asInt


                        //If person added then 1, else send message to user with error 1
                        when (addingResult) {
                            1 -> {
                                keyboard = getKeyboardJson()
                                sendEmail()
                                "Поздравляем, ваша учетня запись прикреплена"
                            }
                            else -> "При добавлении вашей учетной записи произошла ошибка 1"
                        }

                    }

                    //If the auth code (token) was entered incorrectly
                    26 -> "Проверьте правильность написания кода"

                    //If there are any other errors
                    else -> "При добавлении вашей учетной записи произошла ошибка 2"
                }

            } else {
                //if the user has sent a non-template code message
                message = "Если вы пытались прислать код для верификация, то вы сделали это как-то не так\n" +
                        "Я не веду бесед с незнакомцами\n" +
                        "Проверьте правильность написания кода"
            }
        } else if (db.isUserInDBByVkId(vkId).get("result").asString == "unknown") {

            message = when (messageText.startsWith("L:")) {
                true -> "Простите, на данный момент мы не можем вас авторизовать!\n" +
                        "Повторите вашу попытку позже"
                else -> "Я не понимаю, что вы хотели мне сказать."
            }

        } else {
            //If user already authorized in the system
            if (messageText.startsWith("L:")) {

                message = "Ранее вы уже были авторизованы!"
            } else {
                message = when (BotCommands.getEnumClassByCommandText(messageText)) {

                    BotCommands.UnSubscribeEmail -> unSubscribe("email")

                    BotCommands.UnSubscribeVk -> unSubscribe("vk")

                    BotCommands.UnSubscribePhone -> unSubscribe("phone")

                    BotCommands.SubscribeEmail -> subscribe("email")

                    BotCommands.SubscribeVk -> subscribe("vk")

                    BotCommands.SubscribePhone -> subscribe("phone")

                    BotCommands.Help -> {
                        var result = "Возможные комманды:\n"
                        BotCommands.values().forEach {
                            if (it.commandText != "/help")
                                result += it.commandText + "\n"
                        }
                        result
                    }

                    null -> "Я не понимаю, что вы хотите мне сказать"
                }
                keyboard = getKeyboardJson()
            }

        }

        sendMessage(message)

        return resultJson
    }

    private fun unSubscribe(typeNotice: String): String {
        val statusCode = db!!.updatePersonInfo(
            db.isUserInDBByVkId(vkId).get("id").asString,
            mutableMapOf(Pair("${typeNotice}Notice", "false"))
        ).get("statusCode").asInt

        return when (statusCode) {
            1 -> "Вы успешно отписаны от $typeNotice рассылки!"
            else -> "По непонятным причинам вы не были отписаны от $typeNotice рассылки"
        }

    }

    private fun subscribe(typeNotice: String): String {
        val statusCode = db!!.updatePersonInfo(
            db.isUserInDBByVkId(vkId).get("id").asString,
            mutableMapOf(Pair("${typeNotice}Notice", "true"))
        ).get("statusCode").asInt

        return when (statusCode) {
            1 -> "Вы успешно подписаны на $typeNotice рассылку!"
            else -> "По непонятным причинам вы не были подписаны на $typeNotice рассылку"
        }
    }
}
