package com.rtu.itlab.responses.event

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.emailsender.*
import com.rtu.itlab.responses.ResponseHandler
import com.rtu.itlab.responses.event.models.EventView
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventChange(eventView: EventView, db: DBClient) : ResponseHandler(db) {
    private val logger:Logger = LoggerFactory.getLogger("com.rtu.itlab.responses.event.EventChange")
    private val notify = NotifyMessages().event().eventChange(eventView.title).eventInfo(eventView).addUrl(eventView)
    private val users = Users(eventView)

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
        else
            html.changeUrl(config.getString("frontend.host"))

        sendMail(
            UserMail(config.getString("mail.email"), config.getString("mail.password")),
            MailMessage("RTUITLAB NOTIFICATION", html.getHtmlString()),
            HostMail(config.getString("mail.port"), config.getString("mail.host")),
            users.notInvitedUsersEmails.toMutableSet()
        )
    }



    override fun send(): JsonObject {

        if (!usersEmails!!.isEmpty()) {
            logger.info("Invited users, which will notify by email: ${users.invitedUsersEmails}")
            logger.info("Not invited users, , which willnotify by email: ${users.notInvitedUsersEmails}")

            if (users.invitedUsersEmails.isNotEmpty()) {
                EventInvite().sendEmail(users.invitedUsersEmails, notify)
            }

            if (users.notInvitedUsersEmails.isNotEmpty()) {
                sendEmail()
            }
        }

        if (!usersIds!!.isEmpty()) {

            logger.info("Invited users, which will notify by vk: ${users.invitedUsersVks}")
            logger.info("Not invited users, , which will notify by vk: ${users.notInvitedUsersVks}")

            if (users.invitedUsersVks.isNotEmpty()) {
                EventInvite().send(users.invitedUsersVks, notify)
            }

            if (users.notInvitedUsersVks.isNotEmpty())

                vk.messages()
                    .send(actor, users.notInvitedUsersVks)
                    .message(
                        notify.concatenate()
                    ).execute()

            resultJson.addProperty("statusCode", 1)
            logger.info("Info messages about changed event sent to users VK")
        } else {
            resultJson.addProperty("statusCode", 30)
            logger.error("Can't send messages to users,userList for vkNotification is empty!")
        }
        return resultJson
    }


}