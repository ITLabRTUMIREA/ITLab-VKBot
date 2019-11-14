package messageprocessing.responses

import database.HibernateUtil
import messageprocessing.Handler
import messageprocessing.responses.event.EventView
import org.slf4j.LoggerFactory


class EventNew : Handler() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun process(eventView: EventView, databaseConnection: HibernateUtil) {

    }
}