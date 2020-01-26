package messageprocessing.responses

import database.HibernateUtil
import database.schema.UserSettings
import messageprocessing.Handler
import messageprocessing.responses.event.Event
import messageprocessing.responses.event.EventView
import messageprocessing.responses.event.NotifyMessages
import org.slf4j.LoggerFactory
import workwithapi.RequestsToServerApi

class EventConfirm(private val eventView: EventView) : Handler() {
    private val notify =
        NotifyMessages().event().eventConfirm(eventView.data.title).addUrl(eventView)
    val logger = LoggerFactory.getLogger(this::class.java)
    override fun process(
        requestsToServerApi: RequestsToServerApi,
        databaseConnection: HibernateUtil,
        eventView: EventView?,
        notify: Event?
    ) {

        val allUsersInDatabase = databaseConnection.getEntities(UserSettings())?.toMutableList()

        val user = if (this.eventView.data.user != null && allUsersInDatabase != null) {
            logger.info("Getting user from database by id")
            findUserSettingsById(this.eventView.data.user.id, allUsersInDatabase)
        } else {
            logger.debug("User == null or users in database == null")
            null
        }

        if (user != null) {
            logger.info("Sending notification to ${this.eventView.data.user?.id}")
            val notificationType = getNotificationType()
            if (notificationType == 1)
                this.notify.toName(this.eventView.data.user!!.firstName)

            if (user.vkNotification) {
                sendVk(mutableListOf(this.eventView.data.user!!), this.notify)
            }
            if (user.emailNotification) {
                sendEmail(setOf(this.eventView.data.user!!.email!!), this.notify)
            }
        }
    }
}