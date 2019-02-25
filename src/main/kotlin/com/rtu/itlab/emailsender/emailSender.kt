package com.rtu.itlab.emailsender

import com.sun.mail.smtp.SMTPTransport
import org.slf4j.LoggerFactory
import java.util.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.*

private val logger = LoggerFactory.getLogger("com.rtu.itlab.emailsender.emailSender")
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
    userMail: String, password: String, messageSubject: String = "", messageContent: String = "",
    receivers: Set<String>, from: String = userMail, port: String = "465", host: String = "smtp.gmail.com"
) {
    try {
        //system properties
        val properties: Properties = System.getProperties()

        //host
        properties.setProperty("mail.smtps.host", host)
        //class used to create SMTP sockets
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        //setting up port
        properties.setProperty("mail.smtp.port", port)
        //setting up port
        properties.setProperty("mail.smtp.socketFactory.port", port)
        //attempt to authenticate the user using the AUTH command
        properties.setProperty("mail.smtps.auth", "true")

        val session = Session.getDefaultInstance(properties, null)

        val message = MimeMessage(session)
        val transport = session.getTransport("smtps") as SMTPTransport
        transport.connect(host, userMail, password)
        message.setFrom(InternetAddress(from))
        //theme of message
        message.setSubject(messageSubject, "UTF-8")
        //text of message
        message.setContent(messageContent, "text/html; charset = UTF-8")

        for (receiver in receivers) {
            message.addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
        }
        if (!message.getRecipients(Message.RecipientType.TO).isNullOrEmpty())
            transport.sendMessage(message, message.allRecipients)
        logger.info("All messages sent to emails! $receivers")
        transport.close()
    } catch (ex: Exception) {
        logger.error(ex.message + " (EMAIL NOTIFY)")
    }
}

fun sendMail(userMail: UserMail, mailMessage: MailMessage, hostMail: HostMail, receivers: Set<String>) {
    sendMail(
        userMail.email, userMail.password, mailMessage.subject, mailMessage.content, receivers,
        userMail.from, host = hostMail.host, port = hostMail.port
    )
}