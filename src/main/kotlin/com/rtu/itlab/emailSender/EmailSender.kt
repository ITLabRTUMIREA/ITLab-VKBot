package com.rtu.itlab.emailSender

import com.sun.mail.smtp.SMTPTransport
import com.typesafe.config.ConfigFactory

import java.util.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.jsoup.Jsoup
import java.io.File
import javax.mail.*


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

fun sendMail(userMail: String, password: String, messageSubject: String = "", messageContent: String = "",
             receivers: Set<String>, from: String = userMail, port: String = "465", host: String = "smtp.gmail.com") {

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

    transport.sendMessage(message, message.allRecipients)
    println("All messages send!")
    transport.close()
}

fun sendMail(user: User, mailMessage: MailMessage, host: Host, receivers: Set<String>) {
    sendMail(user.email, user.password, mailMessage.subject, mailMessage.content, receivers,
            user.from, host = host.port, port = host.port)
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

fun main(args: Array<String>) {
    var config = ConfigFactory.load()
    sendMail(config.getString("mail.email"), config.getString("mail.password"), "Test message 1", "This message from mail",
            setOf("dmt_98@mail.ru", "drdist@yandex.ru"), host = config.getString("mail.host"))
}


