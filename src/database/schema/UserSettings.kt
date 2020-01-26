package database.schema

import javax.persistence.*

@Entity
@Table(name = "user_settings", schema = "public", catalog = "postgres")
data class UserSettings(
    @Id
    @Column(name = "id", nullable = false)
    val id: String? = null,

    @Basic
    @Column(name = "vk_notification", nullable = false, columnDefinition = "bool default true")
    val vkNotification: Boolean = true,

    @Basic
    @Column(name = "email_notification", nullable = false, columnDefinition = "bool default true")
    val emailNotification: Boolean = true,

    @Basic
    @Column(name = "vk_new_event_notification", nullable = false, columnDefinition = "bool default true")
    val newEventNotification: Boolean = true,

    @Basic
    @Column(name = "vk_change_event_notification", nullable = false, columnDefinition = "bool default true")
    val changeEventNotification: Boolean = true,

    @Basic
    @Column(name = "vk_confirm_event_notification", nullable = false, columnDefinition = "bool default true")
    val confirmEventNotification: Boolean = true

)