package emailsender

import com.sun.mail.smtp.SMTPTransport
import org.slf4j.LoggerFactory
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

private val logger = LoggerFactory.getLogger("emailSender")
/**
 * Method for sending message to mail
 * @param userMail user login(email)
 * @param password user password from email
 * @param messageSubject Topic of the letter
 * @param messageContent Content of the letter (The letter itself)
 * @param receivers List of email addresses to receive email
 * @param from sender's address
 * @param port email service port
 * @param host email host address
 * @return List with lines of file
 */
fun sendMail(
    userMail: String?, password: String?, messageSubject: String? = "", messageContent: String? = "",
    receivers: Set<String>?, from: String? = userMail, port: String? = "465", host: String? = "smtp.gmail.com"
) {
    try {
        val properties: Properties = System.getProperties()
        properties["mail.smtp.host"] = host
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.socketFactory.port"] = port
        properties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        properties["mail.smtp.port"] = port

        val session =
            Session.getDefaultInstance(properties,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication? {
                        return PasswordAuthentication(userMail, password)
                    }
                })

        val message = MimeMessage(session)
        val transport = session.getTransport("smtps") as SMTPTransport
        transport.connect(host, userMail, password)
        message.setFrom(InternetAddress(from))
        message.setSubject(messageSubject, "UTF-8")
        message.setContent(messageContent, "text/html; charset = UTF-8")

        if (receivers != null)
            for (receiver in receivers) {
                message.addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
            }

        if (!message.getRecipients(Message.RecipientType.TO).isNullOrEmpty())
            transport.sendMessage(message,message.allRecipients)
        println("HHHHHHHHHHHHEEEEEEEEEEEERRRRRRRRRRRRREEEEEEEEEEEEEEE")
        logger.info("All messages sent to emails! $receivers")
        transport.close()
    } catch (ex: Exception) {
        logger.error(ex.message)
    }
}

fun sendMail(userMail: UserMail?, mailMessage: MailMessage?, hostMail: HostMail?, receivers: Set<String>?) {
    sendMail(
        userMail?.email, userMail?.password, mailMessage?.subject, mailMessage?.content, receivers,
        userMail?.from, host = hostMail?.host, port = hostMail?.port
    )
}