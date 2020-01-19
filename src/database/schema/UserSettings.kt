package database.schema

import javax.persistence.*

@Entity
@Table(name = "user_settings", schema = "public", catalog = "postgres")
data class UserSettings(
    @Id
    @Column(name = "id")
    val id: String? = null,

    @Basic
    @Column(name = "vk_notification")
    val vkNotification: Boolean = true,

    @Basic
    @Column(name = "email_notification")
    val emailNotification: Boolean = true,

    @Basic
    @Column(name = "vk_new_event_notification")
    val newEventNotification: Boolean = true,

    @Basic
    @Column(name = "vk_change_event_notification")
    val changeEventNotification: Boolean = true,

    @Basic
    @Column(name = "vk_confirm_event_notification")
    val confirmEventNotification: Boolean = true

)