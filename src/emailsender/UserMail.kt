package emailsender

/**
 * @param email user login(email)
 * @param password user password from email
 * @param from sender's address
 */
data class UserMail(val email: String?,
                    val password: String?,
                    val from: String? = email)