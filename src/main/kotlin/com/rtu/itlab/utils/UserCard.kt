package com.rtu.itlab.utils

class UserCard(var userId: Int): Comparable<UserCard>{
    var vkId: Int? = null
    var token: String = ""

    override fun compareTo(other: UserCard): Int {
        return userId-other.userId
    }
}