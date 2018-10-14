package com.rtu.itlab.emailSender

data class User(val email: String, val password: String, val from: String = email)
