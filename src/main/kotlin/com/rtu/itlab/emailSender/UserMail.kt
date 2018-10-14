package com.rtu.itlab.emailSender

data class UserMail(val email: String, val password: String, val from: String = email)
