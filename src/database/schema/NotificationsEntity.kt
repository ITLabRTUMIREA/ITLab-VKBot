package database.schema

import javax.persistence.*

@Entity
@Table(name = "notifications", schema = "public", catalog = "postgres")
data class NotificationsEntity(
    @Id
    @Column(name = "id")
    val id: String? = null,

    @Basic
    @Column(name = "vk_notification")
    val vkNotification: Boolean = false,

    @Basic
    @Column(name = "email_notification")
    val emailNotification: Boolean = false

)