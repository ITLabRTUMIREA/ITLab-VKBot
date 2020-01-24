package messageprocessing

import com.google.gson.JsonObject
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import database.HibernateUtil
import database.schema.UserSettings
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
    private val logger = LoggerFactory.getLogger(this::class.java)


    init {
        val groupId = Config().loadPath("group.id")?.toInt()
        val groupAccessToken = Config().loadPath("group.accessToken")

        if (groupId != null && groupAccessToken != null)
            actor = GroupActor(groupId, groupAccessToken)
        else
            logger.error("groupId or groupAccessToken is null")

    }

    open fun process(inputJson: JsonObject? = null, databaseConnection: HibernateUtil) {}

    /**
     * Function for processing notifications
     * @param requestsToServerApi class for sending requests to host rtuitlab.ru
     * @param databaseConnection class for sending requests to database
     * @param eventView Event about sending notification
     * @param notify class which stored message text
     */
    open fun process(
        requestsToServerApi: RequestsToServerApi,
        databaseConnection: HibernateUtil,
        eventView: EventView? = null,
        notify: Event? = null
    ) {

        val event = requestsToServerApi.getEventById(eventView!!.data.id)
        val invitedUsers = event?.invited()?.toMutableList()

        var allUsersInDatabase = databaseConnection.getEntities(UserSettings())?.toMutableList()

        val allUsersInService = requestsToServerApi.getUsers()?.toMutableList()

        logger.trace("Num of users in database before invite sending = ${allUsersInDatabase?.size}")
        allUsersInDatabase = sendToInvitedUsers(allUsersInDatabase, invitedUsers, notify!!)
        logger.trace("Num of users in database after invite sending = ${allUsersInDatabase?.size}")

        if (!allUsersInDatabase.isNullOrEmpty())
            sendToUsersNotification(allUsersInService, allUsersInDatabase, notify)
    }

    /**
     * Function for getting vk id from user
     * @param user
     * @return vk id if found else null
     */
    open fun getVkId(user: User): Int? {
        val vkId = Config().loadPath("propertiesIds.vk")
        for (it in user.properties) {
            if (it.userPropertyType.id == vkId)
                return it.value.toInt()
            else
                continue
        }

        logger.error("No vkId property found for user with id = ${user.id}")

        return null
    }

    /**
     * Function for loading from config parameters for email notification
     * @return class with parameters
     */
    fun loadConfigurationsForEmail() = EmailConfiguration(
        email = Config().loadPath("mail.email"),
        password = Config().loadPath("mail.password"),
        subject = Config().loadPath("mail.subject"),
        port = Config().loadPath("mail.port"),
        host = Config().loadPath("mail.host")
    )

    open fun sendVk(users: List<User>?, event: Event) {
        val vkIds = mutableListOf<Int>()
        if (users != null)
            for (user in users) {
                val vkId = getVkId(user)
                if (vkId != null)
                    vkIds.add(vkId)
            }
        if (vkIds.size != 0)
            vk.messages()
                .send(actor, vkIds)
                .message(
                    event.concatenate()
                ).execute()

    }


    open fun sendVk(message: String?, vkId: String?) {}

    private fun checkNotification(userSettings: UserSettings, currentNotification: Event) =
        ((userSettings.newEventNotification && currentNotification.params.contains("event_new")) ||
                (userSettings.changeEventNotification && currentNotification.params.contains("event_change")) ||
                (userSettings.confirmEventNotification && currentNotification.params.contains("event_confirm")))


    private fun checkVkNotification(userSettings: UserSettings, currentNotification: Event) =
        checkNotification(userSettings, currentNotification) && userSettings.vkNotification

    private fun checkEmailNotification(userSettings: UserSettings, currentNotification: Event) =
        checkNotification(userSettings, currentNotification) && userSettings.emailNotification

    open fun sendToInvitedUsers(
        allUsersInDatabase: MutableList<UserSettings>?,
        invitedUsers: MutableList<User>?,
        notify: Event
    ): MutableList<UserSettings>? {
        if (!invitedUsers.isNullOrEmpty()) {
            val notificationType = getNotificationType()
            val vkNotifyUsers = mutableSetOf<User>()
            val emailsNotify = mutableSetOf<String>()
            var currentNotification = notify.invite()
            for (it in invitedUsers) {
                var userToDelete: UserSettings? = null

                if (allUsersInDatabase != null) {
                    val user = findUserSettingsById(it.id, allUsersInDatabase)
                    if (user != null) {
                        userToDelete = user
                        if (notificationType == 1) {
                            currentNotification = currentNotification.toName(it.firstName)
                            logger.info("User with id = ${user.id} invited")
                            sendNotificationTypeOne(user, it, currentNotification)

                        } else if (notificationType == 2) {

                            if (checkVkNotification(user, currentNotification))
                                vkNotifyUsers.add(it)

                            if (checkEmailNotification(user, currentNotification))
                                emailsNotify.add(it.email!!)

                        }
                    }

                    if (userToDelete != null && !allUsersInDatabase.isNullOrEmpty())
                        allUsersInDatabase.remove(userToDelete)

                } else {
                    break
                }
            }
            if (notificationType == 2) {
                sendNotificationTypeTwo(vkNotifyUsers, emailsNotify, currentNotification)
            }

        }

        return allUsersInDatabase
    }

    private fun getNotificationType(): Int {
        var notificationType = Config().loadPath("notification.type")?.toIntOrNull()
        if (notificationType == null) {
            notificationType = 2
            logger.trace("Notification type = 2 by default")
        }
        return notificationType
    }

    private fun sendNotificationTypeOne(userSetting: UserSettings, user: User, currentNotification: Event) {
        Thread.sleep(4000)

        if (checkVkNotification(userSetting, currentNotification))
            sendVk(mutableListOf(user), currentNotification)

        if (checkEmailNotification(userSetting, currentNotification))
            sendEmail(setOf(user.email!!), currentNotification)

    }

    private fun sendNotificationTypeTwo(
        vkNotifyUsers: Set<User>,
        emailsNotify: Set<String>,
        currentNotification: Event
    ) {
        if (!vkNotifyUsers.isNullOrEmpty())
            sendVk(vkNotifyUsers.toList(), currentNotification)

        if (!emailsNotify.isNullOrEmpty())
            sendEmail(emailsNotify, currentNotification)
    }

    open fun sendToUsersNotification(
        allUsersInService: MutableList<User>?,
        allUsersInDatabase: MutableList<UserSettings>?,
        notify: Event
    ) {
        if (allUsersInDatabase != null) {
            val notificationType = getNotificationType()
            val vkNotifyUsers = mutableSetOf<User>()
            val emailsNotify = mutableSetOf<String>()
            for (it in allUsersInDatabase) {
                if (allUsersInService != null) {
                    val user = findUserById(it.id!!, allUsersInService)
                    if (user != null) {
                        if (user.id == it.id) {
                            val currentNotification = notify.toName(user.firstName)
                            if (notificationType == 1) {
                                sendNotificationTypeOne(it, user, currentNotification)
                            } else if (notificationType == 2) {
                                if (checkVkNotification(it, currentNotification))
                                    vkNotifyUsers.add(user)

                                if (checkEmailNotification(it, currentNotification))
                                    emailsNotify.add(user.email!!)
                            }
                            allUsersInService.remove(user)
                        }
                    } else {
                        logger.error("User with id = ${user?.id} not found in service database")
                    }
                }
            }
            if (notificationType == 2) {
                sendNotificationTypeTwo(vkNotifyUsers, emailsNotify, notify)
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

    open fun findUserSettingsById(id: String, usersSettings: MutableList<UserSettings>): UserSettings? {
        for (user in usersSettings) {
            if (user.id == id)
                return user
        }
        return null
    }

    /**
     * Function for sending email notifications to emails
     * @param destinationEmails emails on which we sending notification
     * @param event notification information (message)
     */
    open fun sendEmail(destinationEmails: Set<String>, event: Event? = null) {
        val html = HtmlEmail()
        val emailNotify = event!!.getForEmailNotice()

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