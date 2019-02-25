package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.emailsender.*
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.*
import com.rtu.itlab.utils.Config
import com.typesafe.config.ConfigException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Class of sending a message to VC when a new event was created
 * @param eventView
 * @param db - Database with persons info

 */
class EventNew(private val eventView: EventView, db: DBClient) : ResponseHandler(db) {

    private val logger = LoggerFactory.getLogger("com.rtu.itlab.responses.event.EventNew")
    private val notify = NotifyMessages().event().eventNew(eventView.title).eventInfo(eventView).addUrl(eventView)
    private val users = Users(eventView)

    override fun send(): JsonObject {

        if (!usersEmails!!.isEmpty()) {
            logger.info("Invited users, which will notify by email: ${users.invitedUsersEmails}")
            logger.info("Not invited users, , which will notify by email: ${users.notInvitedUsersEmails}")

            if (users.invitedUsersEmails.isNotEmpty()) {
                GlobalScope.launch { EventInvite().sendEmail(users.invitedUsersEmails, notify) }

            }

            if (users.notInvitedUsersEmails.isNotEmpty()) {
                GlobalScope.launch { sendEmail() }
            }
        }

        if (!usersIds!!.isEmpty()) {

            logger.info("Invited users, which will notify by vk: ${users.invitedUsersVks}")
            logger.info("Not invited users, , which will notify by vk: ${users.notInvitedUsersVks}")

            if (users.invitedUsersVks.isNotEmpty()) {
                GlobalScope.launch { EventInvite().send(users.invitedUsersVks, notify) }
            }

            if (users.notInvitedUsersVks.isNotEmpty())

                vk.messages()
                    .send(actor, users.notInvitedUsersVks)
                    .message(
                        notify.concatenate()
                    ).execute()

            resultJson.addProperty("statusCode", 1)
            logger.info("Info messages about new event sent to users VK")
        } else {
            resultJson.addProperty("statusCode", 30)
            logger.error("Can't send messages to users,userList for vkNotification is empty!")
        }



        return resultJson
    }

    override fun sendEmail() {
        val html = HtmlEmail()
        val emailNotify = notify.getForEmailNotice()

        if (emailNotify.contains("description"))
            html.changeDescription(emailNotify["description"]!!)
        else
            html.changeDescription("")

        if (emailNotify.contains("title"))
            html.changeTitle(emailNotify["title"]!!)
        else
            html.changeTitle("")

        if (emailNotify.contains("url"))
            html.changeUrl(emailNotify["url"]!!.removePrefix("Ссылка на событие: "))
        else when (val response = Config().checkPath("frontend.host")) {
            null -> html.changeUrl(config.getString("null"))
            else -> html.changeUrl(response)
        }

        try {
            if (html.getHtmlString().isNotBlank()) {
                sendMail(
                    UserMail(config.getString("mail.email"), config.getString("mail.password")),
                    MailMessage("RTUITLAB NOTIFICATION", html.getHtmlString()),
                    HostMail(config.getString("mail.port"), config.getString("mail.host")),
                    users.notInvitedUsersEmails.toMutableSet()
                )
            } else {
                logger.error("Html is empty or blank. Can't send message to users!")
            }
        } catch (ex: ConfigException) {
            logger.error(ex.message + " (CONFIG)")
        }
    }
}

