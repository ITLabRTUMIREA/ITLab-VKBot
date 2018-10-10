import com.sun.mail.smtp.SMTPTransport
import java.io.*
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.jsoup.Jsoup
import java.io.File


//Read by line from File without duplicates
fun readFromFile(path: String): List<String> {

    val inputStream: InputStream = File(path).inputStream()
    val linesFromFile = mutableListOf<String>()

    inputStream.bufferedReader().useLines { lines -> lines.forEach { linesFromFile.add(it) } }

    //toSet() Delete duplicates
    return linesFromFile.toSet().toList()
}


fun sendMail(user: String, password: String, messageSubject: String, messageContent: String, receivers: List<String>, from: String, messageDelay: Long = 100,
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

        Thread.sleep(messageDelay)

    }
}


fun getWebSiteCode(path: String): String {
    val input = File(path)
    val doc = Jsoup.parse(input, null)
    val html = doc.html()
    return html
}


