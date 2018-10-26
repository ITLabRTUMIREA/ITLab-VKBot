package com.rtu.itlab.database

data class DBUser(var id: String, val firstName: String, val lastName: String,
                  val phoneNumber: String? = null, val email: String? = null,
                  val vkId: String, var vkNotice: Boolean,
                  var emailNotice: Boolean , var phoneNotice: Boolean)