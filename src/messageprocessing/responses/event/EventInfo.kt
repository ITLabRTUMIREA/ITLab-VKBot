package responses.event

import workwithapi.User
import java.util.*

data class InvitedView(val user: User)

data class PlaceView(val targetParticipantsCount: Int, val invited: List<InvitedView>)

data class ShiftView(val beginTime: Date, val endTime: Date, val places: List<PlaceView>)

data class DataView(val id: String, val title: String, val address: String, val shifts: List<ShiftView>)

data class EventView(val type: Int, val data: DataView)

fun EventView.targetParticipantsCount(): Int {
    return this.data.shifts.flatMap { it.places }.sumBy { it.targetParticipantsCount }
}

fun EventView.beginTime(): Date {
    return this.data.shifts.minBy { it.beginTime }!!.beginTime
}

fun EventView.endTime(): Date {
    return this.data.shifts.maxBy { it.endTime }!!.endTime
}

fun EventView.invited(): List<User> {
    return this.data.shifts.flatMap { it.places }.flatMap { it.invited }.map { it.user }
}
