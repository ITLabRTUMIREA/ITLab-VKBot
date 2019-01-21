package com.rtu.itlab.responses

import com.google.gson.JsonObject
import com.rtu.itlab.database.DBClient
import com.rtu.itlab.database.DBUser
import com.rtu.itlab.responses.event.EventInvite
import com.rtu.itlab.responses.event.NotifyMessages
import com.rtu.itlab.responses.event.models.EventView
import com.rtu.itlab.responses.event.models.invited
import com.rtu.itlab.utils.Config
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient


/**
 *Class for work with database, config, vkApi(Getting users Ids, vkApi)
 * @param db database object
 */
abstract class ResponseHandler(val db: DBClient? = null) {
    val config = Config().config!!
    private val transportClient = HttpTransportClient.getInstance()
    val vk = VkApiClient(transportClient)

    /**
     * Status codes:
     * 1-OK
     *
     * 30 - Can't send messages to users,userList for vkNotification is empty!
     */
    val resultJson = JsonObject()

    val actor = GroupActor(config.getInt("group.id"), config.getString("group.accessToken"))

    val usersIds = when (db) {
        null -> null
        else -> db.getUsersVkIdForVkMailing().getAsJsonArray("vkIDs").map { it.asInt }
    }

    val usersEmails = when (db) {
        null -> null
        else -> db.getUsersMailsForEmailMailing().getAsJsonArray("emails").map { it.asString }
    }

    val usersPhones = when (db) {
        null -> null
        else -> db.getUsersPhonesForPhoneMailing().getAsJsonArray("phonesNumbers").map { it.asString }
    }


    /**
     * Class with info about invited and not invited users
     */
    inner class Users(eventView: EventView) {

        val invitedUsersVks = mutableListOf<Int>()
        val notInvitedUsersVks = mutableListOf<Int>()
        val invitedUsersEmails = mutableListOf<String>()
        val notInvitedUsersEmails = mutableListOf<String>()
        val invitedUsersPhones = mutableListOf<String>()
        val notInvitedUsersPhones = mutableListOf<String>()

        init {
            val userIdsWithoutInvite: List<Int>
            val usersEmailsWithoutInvite: List<String>
            val usersPhonesWithoutInvite: List<String>

            if (!usersIds!!.isEmpty()) {

                val invitedUserIds = when (db) {
                    null -> null
                    else -> db.getUsersVkIdForVkMailing(eventView.invited()).getAsJsonArray("vkIDs").map { it.asInt }
                }

                if (!invitedUserIds.isNullOrEmpty()) {
                    userIdsWithoutInvite = usersIds.subtract(invitedUserIds).toList()
                    invitedUsersVks.addAll(invitedUserIds)
                } else {
                    userIdsWithoutInvite = usersIds.toList()
                }

                notInvitedUsersVks.addAll(userIdsWithoutInvite)

            }

            if (!usersEmails!!.isEmpty()) {
                val invitedUserEmails = when (db) {
                    null -> null
                    else -> db.getUsersEmailsForEmailMailing(eventView.invited()).getAsJsonArray("emails").map { it.asString }
                }

                if (!invitedUserEmails.isNullOrEmpty()) {
                    usersEmailsWithoutInvite = usersEmails.subtract(invitedUserEmails).toList()
                    invitedUsersEmails.addAll(invitedUserEmails)
                } else {
                    usersEmailsWithoutInvite = usersEmails.toList()
                }

                notInvitedUsersEmails.addAll(usersEmailsWithoutInvite)
            }

            if (!usersPhones!!.isEmpty()) {
                val invitedUserPhones = when (db) {
                    null -> null
                    else -> db.getUsersPhonesForPhoneMailing(eventView.invited()).getAsJsonArray("phonesNumbers").map { it.asString }
                }

                if (!invitedUserPhones.isNullOrEmpty()) {
                    usersPhonesWithoutInvite = usersPhones.subtract(invitedUserPhones).toList()
                    invitedUsersPhones.addAll(invitedUserPhones)
                } else {
                    usersPhonesWithoutInvite = usersPhones.toList()
                }

                notInvitedUsersPhones.addAll(usersPhonesWithoutInvite)
            }
        }
    }

    abstract fun send(): JsonObject
    abstract fun sendEmail()
}