package com.rtu.itlab.database

data class DBUser(val id: String, val firstName: String, val lastName: String,
                  val phoneNumber: String, val email: String,
                  val vkId: String, val vkNotice: Boolean,
                  val emailNotice: Boolean, val phoneNotice: Boolean)

inline class UserId(val id: String?)