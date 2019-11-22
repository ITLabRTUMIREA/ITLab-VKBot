package messageprocessing

import com.google.gson.JsonObject
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import database.HibernateUtil
import database.models.UserModel
import database.schema.NotificationsEntity
import emailsender.*
import org.slf4j.LoggerFactory
import messageprocessing.responses.event.Event
import messageprocessing.responses.event.EventView
import messageprocessing.responses.event.invited
import utils.Config
import workwithapi.RequestsToServerApi
import workwithapi.User

abstract class Handler {
    private val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)
    var actor: GroupActor? = null
    private val logger = LoggerFactory.getLogger(this.javaClass.name)


    init {
        val groupId = Config().loadPath("group.id")?.toInt()
        val groupAccessToken = Config().loadPath("group.accessToken")

        if (groupId != null && groupAccessToken != null)
            actor = GroupActor(groupId, groupAccessToken)
        else
            logger.error("groupId or groupAccessToken is null")

    }

    open fun process(inputJson: JsonObject? = null, databaseConnection: HibernateUtil) {

    }

    open fun process(
        requestsToServerApi: RequestsToServerApi,
        databaseConnection: HibernateUtil,
        eventView: EventView? = null,
        notify: Event? = null
    ) {

        val event = requestsToServerApi.getEventById(eventView!!.data.id)
        val invitedUsers = event?.invited()?.toMutableList()

        var allUsersInDatabase = databaseConnection.getEntities(NotificationsEntity())?.toMutableList()

        val allUsersInService = requestsToServerApi.getUsers()?.toMutableList()

        logger.info("Num of users in database before invite sending = ${allUsersInDatabase?.size}")
        allUsersInDatabase = sendToInvitedUsers(allUsersInDatabase, invitedUsers, notify!!)
        logger.info("Num of users in database after invite sending = ${allUsersInDatabase?.size}")

        if (!allUsersInDatabase.isNullOrEmpty())
            sendToUsersNotification(allUsersInService, allUsersInDatabase, notify)
    }

    open fun getVkId(user: User): Int? {
        for (it in user.properties) {
            if (it.userPropertyType.title == "VKID")
                return it.value.toInt()
            else
                continue
        }

        logger.error("No vkId property found for user with id = ${user.id}")

        return null
    }

    fun loadConfigurationsForEmail(): EmailConfiguration = EmailConfiguration(
        email = Config().loadPath("mail.email"),
        password = Config().loadPath("mail.password"),
        subject = Config().loadPath("mail.subject"),
        port = Config().loadPath("mail.port"),
        host = Config().loadPath("mail.host")
    )

    open fun sendVk(user: User, event: Event) {
        val vkId = getVkId(user)
        if (vkId != null)
            vk.messages()
                .send(actor, vkId)
                .message(
                    event.concatenate()
                ).execute()
    }

    open fun sendToInvitedUsers(
        allUsersInDatabase: MutableList<NotificationsEntity>?,
        invitedUsers: MutableList<User>?,
        notify: Event
    ): MutableList<NotificationsEntity>? {
        val invited = mutableListOf<NotificationsEntity>()
        if (!allUsersInDatabase.isNullOrEmpty()) {

            for (it in allUsersInDatabase) {
                var userToDelete: User? = null
                if (invitedUsers != null) {
                    val user = findUserById(it.id!!, invitedUsers)
                    if (user != null) {
                        userToDelete = user
                        val currentNotification = notify.invite().toName(user.firstName)
                        logger.info("User with id = ${user.id} invited")

                        if (it.vkNotification) {
                            sendVk(user, currentNotification)
                        }
                        if (it.emailNotification) {
                            sendEmail(setOf(user.email!!), currentNotification)
                        }
                        invited.add(it)
                    }

                    if (userToDelete != null && !invitedUsers.isNullOrEmpty())
                        invitedUsers.remove(userToDelete)

                } else {
                    break
                }
            }
        }

        return allUsersInDatabase?.minus(invited)?.toMutableList()
    }


    open fun sendToUsersNotification(
        allUsersInService: MutableList<User>?,
        allUsersInDatabase: MutableList<NotificationsEntity>?,
        notify: Event
    ) {
        if (allUsersInDatabase != null) {
            for (it in allUsersInDatabase) {
                if (allUsersInService != null) {
                    val user = findUserById(it.id!!, allUsersInService)
                    if (user != null) {
                        if (user.id == it.id) {
                            val currentNotify = notify.toName(user.firstName)
                            if (it.vkNotification) {
                                sendVk(user, currentNotify)
                            }
                            if (it.emailNotification) {
                                sendEmail(setOf(user.email!!), currentNotify)
                            }
                            allUsersInService.remove(user)
                        }
                    } else {
                        logger.error("User with id = ${user?.id} not found in service database")
                    }
                }
            }
        }
    }

    open fun findUserById(id: String, users: MutableList<User>): User? {
        for (user in users) {
            if (user.id == id)
                return user
        }
        return null
    }

    open fun sendEmail(destinationEmails: Set<String>, event: Event) {
        val html = HtmlEmail()
        val emailNotify = event.getForEmailNotice()

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
        else when (val response = Config().loadPath("apiserver.host")) {
            null -> html.changeUrl("null")
            else -> html.changeUrl(response)
        }

        val emailConfiguration = loadConfigurationsForEmail()

        if (emailConfiguration.email != null && emailConfiguration.password != null &&
            emailConfiguration.subject != null && emailConfiguration.port != null &&
            emailConfiguration.host != null
        ) {
            if (html.getHtmlString().isNotBlank()) {
                sendMail(
                    UserMail(emailConfiguration.email, emailConfiguration.password),
                    MailMessage(emailConfiguration.subject, html.getHtmlString()),
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
}