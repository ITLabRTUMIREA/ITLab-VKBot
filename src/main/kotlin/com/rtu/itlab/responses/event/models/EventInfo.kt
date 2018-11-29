package com.rtu.itlab.responses.event.models

import com.rtu.itlab.database.DBUser
import java.util.*

data class InvitedView(val user: DBUser)

data class PlaceView(val targetParticipantsCount: Int, val invited: List<InvitedView>)

data class ShiftView(val beginTime: Date, val endTime: Date, val places: List<PlaceView>)

data class EventView(val id: String, val title: String, val address: String, val shifts: List<ShiftView>)

fun EventView.targetParticipantsCount(): Int {
    return this.shifts.flatMap { it.places }.sumBy { it.targetParticipantsCount }
}

fun EventView.beginTime(): Date {
    return this.shifts.minBy { it.beginTime }!!.beginTime
}

fun EventView.endTime(): Date {
    return this.shifts.maxBy { it.endTime }!!.endTime
}

fun EventView.invited(): List<DBUser> {
    return this.shifts.flatMap { it.places }.flatMap { it.invited }.map { it.user }
}
