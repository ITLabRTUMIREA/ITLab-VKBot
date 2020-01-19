package messageprocessing.responses.event

import utils.Config
import java.text.SimpleDateFormat
import java.util.*

class NotifyMessages {

    fun event(): Event {
        return Event()
    }

}

class Event : Builder() {

    /**
     * 1
     */
    fun toName(name: String?): Event {
        return when (name.isNullOrEmpty()) {
            false -> addParams("user_name", name) as Event
            else -> this
        }

    }

    /**
     * 3
     */
    fun invite(title: String): Event {
        val text = "Вы приглашены для участия в событии «${title}»"
        return addParams("event_invite", text) as Event
    }

    /**
     * 3
     */
    fun invite(): Event {
        val text = "Вы приглашены для участия в событии"
        return addParams("event_invite", text) as Event
    }

    /**
     * 4
     */
    fun eventInfo(eventView: EventView): Event {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Moscow")
        val text = "Необходимое количество участников: ${eventView.targetParticipantsCount()}" +
                "\nНачало: ${dateFormat.format(eventView.beginTime().time)} по МСК" +
                "\nОкончание: ${dateFormat.format(eventView.endTime().time)} по МСК" +
                "\nАдрес: ${eventView.data.address}"

        return addParams("event_info", text) as Event
    }

    /**
     * 2
     */
    fun eventNew(title: String): Event {
        val text = "Создано новое событие «${title}»"
        return addParams("event_new", text) as Event
    }

    /**
     * 2
     */
    fun eventChange(title: String): Event {
        val text = "Изменено событие «${title}»"
        return addParams("event_change", text) as Event
    }

    /**
     * 2
     */
    fun eventConfirm(title: String): Event {
        val text = "Подтверждение вышего участия в событии «${title}»"
        return addParams("event_confirm", text) as Event
    }

    /**
     * 4
     */
    fun addUrl(eventView: EventView): Event {
        return addParams(
            "event_url",
            "Ссылка: ${Config().loadPath("apiserver.host")}/events/${eventView.data.id}"
        ) as Event
    }

    /**
     * Concatenating parameters for vk notify
     * @return concatenated string, which we can send
     */
    fun concatenate(): String {
        var result = ""

        if (params.contains("user_name"))
            result += params["user_name"] + "\n"

        if (params.contains("event_new"))
            result += "&#10133; " + params["event_new"] + "\n"

        if (params.contains("event_change"))
            result += "&#10135; " + params["event_change"] + "\n"

        if (params.contains("event_confirm"))
            result += "&#10004; " + params["event_confirm"] + "\n"

        if (params.contains("event_invite"))
            result += params["event_invite"] + "\n"

        if (params.contains("event_info"))
            result += params["event_info"] + "\n"

        if (params.contains("event_url"))
            result += params["event_url"] + "\n"

        return result
    }

    /**
     * Getting parameters for email notify
     * @return map that contain title, description and url
     */
    fun getForEmailNotice(): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()

        if (params.contains("user_name"))
            result["description"] = "<p>" + params["user_name"]!! + "!</p>"

        if (params.contains("event_new"))
            result["title"] = params["event_new"]!!

        if (params.contains("event_change"))
            result["title"] = params["event_change"]!!

        if (params.contains("event_confirm"))
            result["title"] = params["event_confirm"]!!

        if (params.contains("event_invite")) {
            if (result.contains("title"))
                result["title"] = "<p>" + result["title"] + "</p>" + "<p>" + params["event_invite"]!! + "</p>"
            else
                result["title"] = params["event_invite"]!!
        }

        if (params.contains("event_info")) {
            var resultStr = ""
            if (result.contains("description"))
                resultStr += result["description"]
            params["event_info"]!!.lines().forEach { line ->
                resultStr += "<p>$line</p>"
            }
            result["description"] = resultStr
        }

        if (params.contains("event_url"))
            result["url"] = params["event_url"]!!

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