package emailsender

data class EmailConfiguration(
    val email: String?,
    val password: String?,
    val subject: String?,
    val port: String?,
    val host: String?
)