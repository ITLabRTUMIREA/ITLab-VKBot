package messageprocessing.responses

import database.HibernateUtil
import messageprocessing.Handler
import messageprocessing.responses.event.Event
import messageprocessing.responses.event.EventView
import messageprocessing.responses.event.NotifyMessages
import workwithapi.RequestsToServerApi

class EventChange(private val eventView: EventView) : Handler() {

    private val notify =
        NotifyMessages().event().eventChange(eventView.data.title).eventInfo(eventView).addUrl(eventView)

    override fun process(
        requestsToServerApi: RequestsToServerApi,
        databaseConnection: HibernateUtil,
        eventView: EventView?,
        notify: Event?
    ) {
        super.process(requestsToServerApi, databaseConnection, this.eventView, this.notify)
    }
}