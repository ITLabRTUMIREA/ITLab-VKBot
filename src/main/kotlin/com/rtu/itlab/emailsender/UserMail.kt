package com.rtu.itlab.emailsender

data class UserMail(val email: String, val password: String, val from: String = email)