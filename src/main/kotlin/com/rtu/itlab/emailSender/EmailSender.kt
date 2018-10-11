import com.sun.mail.smtp.SMTPTransport

import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.jsoup.Jsoup
import java.io.File


/**
 * Method for sending message to mail
 * @param user user login(email)
 * @param password user password from email
 * @param messageSubject Topic of the letter
 * @param messageContent Content of the letter (The letter itself)
 * @param receivers List of email addresses to receive email
 * @param from sender's address
 * @param messageDelay Delay between sending messages
 * @param port email service port
 * @param host email host address
 *
 * @return List with lines of file
 */

fun sendMail(user: String, password: String, messageSubject: String, messageContent: String, receivers: Set<String>, from: String, messageDelay: Long = 1,
             port: String = "8080", host: String = "") {
    //system properties
    var properies: Properties = System.getProperties()
    //host
    properies.setProperty("mail.smtps.host", host)
    //class used to create SMTP sockets
    properies.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    //ability to create a socket using the specified socket factory class
    properies.setProperty("mail.smtp.socketFactory.fallback", "false")
    //setting up port
    properies.setProperty("mail.smtp.port", port)
    //setting up port
    properies.setProperty("mail.smtp.socketFactory.port", port)
    //attempt to authenticate the user using the AUTH command
    properies.setProperty("mail.smtps.auth", "true")
    //the QUIT command is sent and the connection is immediately closed
    properies.put("mail.smtps.quitwait", "false")


    for (reciever in receivers) {

        var session: Session = Session.getDefaultInstance(properies, null)

        var message: MimeMessage = MimeMessage(session)
        message.setFrom(InternetAddress(from))
        //theme of message
        message.setSubject(messageSubject, "UTF-8")
        //text of message
        message.setContent(messageContent, "text/html; charset = UTF-8")
        var t: SMTPTransport = session.getTransport("smtps") as SMTPTransport

        t.connect(host, user, password)

        message.addRecipient(Message.RecipientType.TO, InternetAddress(reciever))
        t.sendMessage(message, message.allRecipients)

        println("Successfully sent to $reciever")

        t.close()

    }
}

/**
 * This method for reading html page
 * @param path path to file
 * @return html code of page
 */
fun getWebSiteCode(path: String): String {
    val input = File(path)
    val doc = Jsoup.parse(input, null)
    val html = doc.html()
    return html
}


