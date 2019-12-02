package emailsender

/**
 * @param subject Topic of the letter
 * @param content Content of the letter (The letter itself)
 */
data class MailMessage(val subject: String, val content: String)