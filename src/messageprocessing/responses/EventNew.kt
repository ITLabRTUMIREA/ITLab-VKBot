package messageprocessing.responses

import database.HibernateUtil
import messageprocessing.Handler
import messageprocessing.responses.event.Event
import messageprocessing.responses.event.EventView
import messageprocessing.responses.event.NotifyMessages
import workwithapi.RequestsToServerApi

class EventNew(private val eventView: EventView) : Handler() {

    //private val logger = LoggerFactory.getLogger(this::class.java)
    private val notify = NotifyMessages().event().eventNew(eventView.data.title).eventInfo(eventView).addUrl(eventView)

    override fun process(
        requestsToServerApi: RequestsToServerApi,
        databaseConnection: HibernateUtil,
        eventView: EventView?,
        notify: Event?
    ) {
        super.process(requestsToServerApi, databaseConnection, this.eventView, this.notify)
    }
}