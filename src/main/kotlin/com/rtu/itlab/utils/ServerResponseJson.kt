package com.rtu.itlab.utils

import com.rtu.itlab.database.DBUser

data class ServerResponseJson(val data: DBUser?, val statusCode: Int)