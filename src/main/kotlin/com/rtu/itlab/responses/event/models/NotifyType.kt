package com.rtu.itlab.responses.event.models

enum class NotifyType(val statusCode:Int) {
    EventNew(0),
    EventChange(1),
    EventConfirm(2)
}