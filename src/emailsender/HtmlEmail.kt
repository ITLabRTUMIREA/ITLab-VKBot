package emailsender

import com.typesafe.config.ConfigException
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import utils.Config
import java.io.File
import java.io.FileNotFoundException

class HtmlEmail {

    private var pathToHtmlEmail = ""
    private var htmlCode: String = ""
    private val logger = LoggerFactory.getLogger("com.rtu.itlab.emailsender.HtmlEmail")

    init {
        val config = Config().config!!
        try {
            pathToHtmlEmail += config.getString("htmlEmail.path")
            htmlCode += getWebSiteCode(pathToHtmlEmail)
        } catch (ex: ConfigException) {
            logger.info(ex.message + " (Config)")
        }
    }

    /**
     * This method for reading html page
     * @param path path to file
     * @return html code of page
     */
    private fun getWebSiteCode(path: String): String {

        var result = ""

        try {
            val input = File(path)
            val doc = Jsoup.parse(input, null)
            result += doc.html()
            logger.info("Html page loaded")
        } catch (ex: FileNotFoundException) {
            logger.error(ex.message + " (HTML)")
        }

        return result
    }

    /**
     * Changing title in htmlPage {{notifyTitle}}
     * @param title new title
     */
    fun changeTitle(title: String) {
        htmlCode = htmlCode.replace("{{notifyTitle}}", title)
        logger.info("Title in HTML changed")
    }

    /**
     * Changing description in htmlPage {{notifyDescription}}
     * @param description new description
     */
    fun changeDescription(description: String) {
        htmlCode = htmlCode.replace("{{notifyDescription}}", description)
        logger.info("Description in HTML changed")
    }

    /**
     * Changing description in htmlPage {{urlToEvent}}
     * @param url new url
     */
    fun changeUrl(url: String) {
        htmlCode = htmlCode.replace("{{urlToEvent}}", url)
        logger.info("URL in HTML changed")
    }

    fun getHtmlString(): String = htmlCode

}