package messageprocessing

import com.google.gson.JsonObject
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import database.HibernateUtil
import database.models.UserModel
import emailsender.*
import org.slf4j.LoggerFactory
import messageprocessing.responses.event.Event
import messageprocessing.responses.event.EventView
import utils.Config

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

    open fun process(inputJson: JsonObject? = null, databaseConnection: HibernateUtil, event: Event? = null) {}

    open fun process(eventView: EventView, databaseConnection: HibernateUtil){}

    open fun process(invitedUser: UserModel, databaseConnection: HibernateUtil? = null, event: Event? = null) {}

    //open fun process(invitedUsers: List<Int>, databaseConnection: HibernateUtil, messageprocessing.responses.event: Event? = null) {}

    open fun getVkId(user: UserModel): Int? {
        for (it in user.properties) {
            if (it.userPropertyType.title == "vkId")
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
        else when (val response = Config().loadPath("frontend.host")) {
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