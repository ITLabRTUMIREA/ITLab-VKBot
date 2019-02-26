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
import com.rtu.itlab.utils.Config
import com.typesafe.config.ConfigException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Status codes:
 * 1-error while adding to database
 * 2-incorrect status code from server
 * 3-error sending request to server
 */

class VKMessageHandling(tmp: JsonObject?, db: DBClient) : ResponseHandler(db) {

    private val logger = LoggerFactory.getLogger("com.rtu.itlab.responses.VKMessageHandling")

    override fun sendEmail() {
        val html = HtmlEmail()

        when (val response = Config().checkPath("frontend.host")) {
            null -> logger.error("There is no path 'frontend.host' in the config file")
            else -> html.run {
                changeDescription(
                    "<p>Ваш аккаунт сайта $response был привязан к системе уведомлений</p>" +
                            "<p>Управлять(отключать или включать) уведомлениями вы можете путем общения с ботом RTU IT Lab</p>"
                )
                changeUrl(response)
            }
        }

        html.changeTitle("Уведомление об успешном подключении системы уведомлений")

        val userEmail = db!!.getDbUserByKey(db.isUserInDBByVkId(vkId).get("id").asString)!!.email!!

        try {
            if (html.getHtmlString().isNotBlank()) {
                sendMail(
                    UserMail(config.getString("mail.email"), config.getString("mail.password")),
                    MailMessage(
                        "RTUITLAB NOTIFICATION",
                        html.getHtmlString().replace("Перейти к событию", "Перейти на сайт")
                    ),
                    HostMail(config.getString("mail.port"), config.getString("mail.host")),
                    setOf(userEmail)
                )
            } else {
                logger.error("Html is empty or blank. Can't send message to users!")
            }
        } catch (ex: ConfigException) {
            logger.error(ex.message + " (CONFIG)")
        }
    }

    private val vkId = tmp!!.getAsJsonObject("object").get("from_id").asInt
    private val messageText: String = tmp!!.getAsJsonObject("object").get("text").asString
    private var keyboard = "{\"buttons\":[],\"one_time\":true}"

    private fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            vk.messages()
                .send(actor)
                .userId(vkId)
                .message(message)
                .keyboard(keyboard)
                .execute()

        }
    }

    private fun getKeyboardJson(): String {
        var result = ""
        val keyboardClass = getKeyboardForCurrentPerson(vkId, db!!)
        if (keyboardClass.lines.size > 0)
            result = keyboardClass.getKeyboardJson().toString()
        return if (result.isNotBlank())
            result
        else
            "{\"buttons\":[],\"one_time\":true}"
    }


    override fun send(): JsonObject {
        var message = ""
        if (db!!.isUserInDBByVkId(vkId).get("result").asString != "true") {
            if (messageText.startsWith("L:")) {
                var responseObject: ServerResponseJson? = null
                try {
                    val (_, _, result) = Fuel.post(config.getString("apiserver.host") + "/api/account/property/vk").body(
                        Gson().toJson(UserCard(messageText.substringAfter("L:"), vkId))
                    ).header(
                        "Content-Type" to "application/json",
                        "Authorization" to config.getString("apiserver.accessToken")
                    ).responseObject<ServerResponseJson>()

                    if (result.component2() == null && result.get().data != null)
                        responseObject = result.get()
                    else
                        logger.error("Fuel error (Returned null from server)")

                } catch (ex: Exception) {
                    logger.error(ex.message)
                }

                if (responseObject != null) {
                    if (responseObject.statusCode == 1) {

                        //Getting result of adding person to database
                        val addingResult = db.addPerson(
                            responseObject.data!!.copy(
                                vkId = vkId.toString(), vkNotice = true,
                                emailNotice = true, phoneNotice = true
                            )
                        ).get("statusCode").asInt

                        //If person added then 1
                        if (addingResult == 1) {
                            keyboard = getKeyboardJson()
                            GlobalScope.launch { sendEmail() }
                            message = "Поздравляем, ваша учетня запись прикреплена"
                        }
                    }
                }
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

                    BotCommands.DeleteFromNotifyCenter -> deleteFromNotify()

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

    private fun deleteFromNotify(): String {
        val userId = db!!.isUserInDBByVkId(vkId).get("id").asString
        val result = db.deletePerson(userId).get("statusCode").asInt
        return if (result == 1) {
            "Все ваши аккаунты отвязаны и удалены из рассылки"
        } else {
            "Произошла ошибка отвязки/удаления аккаунта statusCode = ${result}"
        }
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
