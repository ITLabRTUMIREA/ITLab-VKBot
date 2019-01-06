package com.rtu.itlab.responses.event

import com.rtu.itlab.responses.event.models.EventView
import com.rtu.itlab.responses.event.models.beginTime
import com.rtu.itlab.responses.event.models.endTime
import com.rtu.itlab.responses.event.models.targetParticipantsCount
import com.rtu.itlab.utils.Config

class NotifyMessages {

    fun event(): Event {
        return Event()
    }

}

class Event : Builder() {

    /**
     * 1
     */
    fun toName(name: String): Event {
        return when (name.isNullOrEmpty()) {
            false -> addParams("user_name", name) as Event
            else -> this
        }

    }

    /**
     * 3
     */
    fun invite(title: String): Event {
        val text = "Вы были приглашены на событие «${title}»!"
        return addParams("event_invite", text) as Event
    }

    /**
     * 3
     */
    fun invite(): Event {
        val text = "Вы были приглашены на данное событие!"
        return addParams("event_invite", text) as Event
    }

    /**
     * 4
     */
    fun eventInfo(eventView: EventView): Event {

        val config = Config().config!!

        val text = "Необходимое количество участников: ${eventView.targetParticipantsCount()}" +
                "\nНачало: ${eventView.beginTime()}" +
                "\nОкончание: ${eventView.endTime()}" +
                "\nАдрес проведения мероприятия: ${eventView.address}" +
                "\nСсылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}"

        return addParams("event_info", text) as Event
    }

    /**
     * 2
     */
    fun eventNew(title: String): Event {
        val text = "Было создано новое событие «${title}»"
        return addParams("event_new", text) as Event
    }

    /**
     * 2
     */
    fun eventChange(title: String): Event {
        val text = "Событие «${title}» было изменено!"
        return addParams("event_change", text) as Event
    }

    /**
     * 2
     */
    fun eventConfirm(title: String): Event {
        val text = "Ваше участие в событии «${title}» подтверждено!"
        return addParams("event_confirm", text) as Event
    }

    /**
     * 4
     */
    fun addUrl(eventView: EventView): Event {
        val config = Config().config!!
        return addParams(
            "event_url",
            "Ссылка на событие: ${config.getString("frontend.host")}/events/${eventView.id}"
        ) as Event
    }

    fun concatenate(): String {
        var result = ""

        if (params.contains("user_name"))
            result += params["user_name"] + "\n"

        if (params.contains("event_new"))
            result += params["event_new"] + "\n"

        if (params.contains("event_change"))
            result += params["event_change"] + "\n"

        if (params.contains("event_confirm"))
            result += params["event_confirm"] + "\n"

        if (params.contains("event_invite"))
            result += params["event_invite"] + "\n"

        if (params.contains("event_info"))
            result += params["event_info"] + "\n"

        if (params.contains("event_url"))
            result += params["event_url"] + "\n"

        return result
    }

}

abstract class Builder {
    val params = mutableMapOf<String, String>()

    fun addParams(key: String, value: String): Builder {
        params[key] = value
        return this
    }

}