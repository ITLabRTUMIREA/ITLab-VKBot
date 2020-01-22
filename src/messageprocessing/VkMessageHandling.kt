package messageprocessing

import bot.keyboard.getKeyboardForCurrentPerson
import bot.keyboard.keyboard
import com.google.gson.JsonObject
import bot.BotCommands
import database.HibernateUtil
import database.schema.UserSettings
import emailsender.*
import org.slf4j.LoggerFactory
import messageprocessing.responses.event.Event
import utils.Config
import workwithapi.RequestsToServerApi

class VKMessageHandling(private val requestsToServerApi: RequestsToServerApi) : Handler() {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    private var email: String? = null

    private var id: String? = null

    private var keyboard = "{\"buttons\":[],\"one_time\":true}"


    override fun sendEmail(destinationEmails: Set<String>, event: Event?) {
        logger.trace("Sending email to user")
        val html = HtmlEmail()

        when (val apiUrl = Config().loadPath("apiserver.host")) {
            null -> logger.error("There is no path 'apiserver.host' in the config file")
            else -> html.run {
                changeDescription(
                    "<p>Ваш аккаунт сайта $apiUrl был привязан к системе уведомлений</p>" +
                            "<p>Управлять(отключать или включать) уведомлениями вы можете путем общения с ботом RTU IT Lab</p>"
                )
                changeUrl(apiUrl)
            }
        }

        html.changeTitle("Уведомление об успешном подключении системы уведомлений")

        val emailConfiguration = loadConfigurationsForEmail()

        if (emailConfiguration.email != null && emailConfiguration.password != null &&
            emailConfiguration.subject != null && emailConfiguration.port != null &&
            emailConfiguration.host != null
        ) {

            if (html.getHtmlString().isNotBlank()) {

                sendMail(
                    UserMail(emailConfiguration.email, emailConfiguration.password),
                    MailMessage(
                        emailConfiguration.subject,
                        html.getHtmlString().replace("Перейти к событию", "Перейти на сайт")
                    ),
                    HostMail(emailConfiguration.port, emailConfiguration.host),
                    destinationEmails
                )
            } else {
                logger.error("Html is empty or blank. Can't send message to users!")
            }

        } else {
            logger.error("Check mail.email, mail.password, mail.subject, mail.port, mail.host in config file")
        }
    }

    override fun sendVk(message: String?, vkId: String?) {
        if (!message.isNullOrEmpty() && !vkId.isNullOrEmpty()) {
            vk.messages()
                .send(actor)
                .userId(vkId.toInt())
                .message(message)
                .keyboard(keyboard)
                .execute()

        }
    }

    private fun getKeyboardJson(vkId: String, databaseConnection: HibernateUtil): String {
        logger.trace("Getting keyboard for user")
        var result = ""

        val keyboardClass = getKeyboardForCurrentPerson(requestsToServerApi, vkId, databaseConnection)

        if (keyboardClass.lines.size > 0)
            result = keyboardClass.getKeyboardJson().toString()

        return if (result.isNotBlank())
            result
        else
            "{\"buttons\":[],\"one_time\":true}"
    }

    override fun process(inputJson: JsonObject?, databaseConnection: HibernateUtil) {
        logger.trace("Processing message")
        val vkId = inputJson?.getAsJsonObject("object")?.get("from_id")?.asString
        val messageText = inputJson?.getAsJsonObject("object")?.get("text")?.asString

        var userModel = if (!vkId.isNullOrEmpty())
            requestsToServerApi.getUserModelByVkId(vkId)
        else
            null

        val message = if (userModel != null && !messageText.isNullOrEmpty() && !vkId.isNullOrEmpty()) {
            id = userModel.id
            email = userModel.email
            if (databaseConnection.isUserInDatabase(id!!)) {
                keyboard = getKeyboardJson(vkId, databaseConnection)

                if (messageText.startsWith("L:"))
                    "Вы уже авторизованы в этом сервисе &#10084;"
                else {
                    val msg = when (BotCommands.getEnumClassByCommandText(messageText)) {

                        BotCommands.SubscribeEmail -> changeSubscriptionStatus("email", databaseConnection)

                        BotCommands.SubscribeVk -> changeSubscriptionStatus("vk", databaseConnection)

                        BotCommands.SubscribeChangeEvent -> changeSubscriptionStatus(
                            "change_event",
                            databaseConnection
                        )

                        BotCommands.SubscribeConfirmEvent -> changeSubscriptionStatus(
                            "confirm_event"
                            , databaseConnection
                        )

                        BotCommands.SubscribeNewEvent -> changeSubscriptionStatus(
                            "new_event",
                            databaseConnection
                        )

                        BotCommands.DeleteFromNotifyCenter
                        -> deleteFromNotify(databaseConnection)

                        BotCommands.Help -> {
                            var result = "Комманды, которые я знаю: \n"
                            BotCommands.values().forEach {
                                if (it.commandText != "/help")
                                    result += it.commandText + "\n"
                            }
                            result
                        }

                        null ->
                            "Я вас не понимаю, то, что я понимаю вы можете узнать написав комманду \"/help\""

                    }
                    keyboard = getKeyboardJson(vkId, databaseConnection)
                    msg
                }
            } else {
                val res = databaseConnection.addEntity(UserSettings(id))

                if (res) {
                    keyboard = getKeyboardJson(vkId, databaseConnection)
                    "Вы добавлены в базу данных рассылки &#128519;"
                } else {
                    keyboard = "{\"buttons\":[],\"one_time\":true}"
                    "Ранее вы уже добавляли vk id на сайт, но произошла ошибка " +
                            "с добавлением вас в базу данных &#128546;"
                }
            }
        } else if (!messageText.isNullOrEmpty()) {
            if (messageText.startsWith("L:")) {
                userModel = requestsToServerApi.sendTokenToServerForAccess(messageText, vkId!!)

                if (userModel != null) {
                    id = userModel.id
                    email = userModel.email
                }

                if (id != null && email != null) {
                    val res = databaseConnection.addEntity(UserSettings(id))
                    if (res) {
                        keyboard = getKeyboardJson(vkId, databaseConnection)
                        sendEmail(setOf(email!!))
                        "Поздравляем!, вы авторизовались в этом сервисе &#128293;&#128293;&#128293;"
                    } else {
                        keyboard = "{\"buttons\":[],\"one_time\":true}"
                        "Произошла ошибка во время добавления вас в базу данных &#128546;"
                    }
                } else {
                    keyboard = "{\"buttons\":[],\"one_time\":true}"
                    "Произошла ошибка во время вашей авторизации. Возможно не верный код авторизации &#128546;"
                }
            } else {
                keyboard = "{\"buttons\":[],\"one_time\":true}"
                "Я не понимаю вас. Возможно, для начала вам нужно " +
                        "авторизоваться в сервисе"
            }
        } else {
            null
        }
        sendVk(message, vkId)
    }

    private fun deleteFromNotify(databaseConnection: HibernateUtil): String {
        logger.debug("Deleting user from notify service")

        return if (!id.isNullOrBlank() &&
            databaseConnection.deleteEntities(id!!, UserSettings())
        ) {
            "Ваши данные были удалины из базы данных данного сервиса"
        } else {
            "Произошла ошибка отвязки вашего аккаунта от данного сервиса"
        }
    }

    private fun changeSubscriptionStatus(typeNotice: String, databaseConnection: HibernateUtil): String {
        logger.trace("Changing $typeNotice status")
        val personInfo = if (!id.isNullOrBlank())
            databaseConnection.getEntityById(id!!, UserSettings())
        else
            null

        return if (personInfo != null) {
            var status = false
            val result = when (typeNotice) {
                "vk" -> {
                    status = !personInfo.vkNotification
                    val newPersonInfo = personInfo.copy(vkNotification = status)
                    databaseConnection.updateEntity(newPersonInfo)
                }
                "email" -> {
                    status = !personInfo.emailNotification
                    val newPersonInfo = personInfo.copy(emailNotification = status)
                    databaseConnection.updateEntity(newPersonInfo)
                }
                "new_event" -> {
                    status = !personInfo.newEventNotification
                    val newPersonInfo = personInfo.copy(newEventNotification = status)
                    databaseConnection.updateEntity(newPersonInfo)
                }

                "change_event" -> {
                    status = !personInfo.changeEventNotification
                    val newPersonInfo = personInfo.copy(changeEventNotification = status)
                    databaseConnection.updateEntity(newPersonInfo)
                }

                "confirm_event" -> {
                    status = !personInfo.confirmEventNotification
                    val newPersonInfo = personInfo.copy(confirmEventNotification = status)
                    databaseConnection.updateEntity(newPersonInfo)
                }
                else -> false
            }

            if (result)
                "Статус $typeNotice рассылки изменен: ${if (status) "Подписан" else "Отписан"}"
            else
                "Произошла ошибка при обновлении ваших данных $typeNotice рассылки"
        } else
            "Произошла ошибка при получении ваших данных из базы данных"

    }

}