package messageprocessing.responses.event

import workwithapi.User
import java.util.*

data class InvitedView(val user: User)

data class PlaceView(val targetParticipantsCount: Int, val invited: List<InvitedView>)

data class ShiftView(val beginTime: Date, val endTime: Date, val places: List<PlaceView>)

data class DataView(val id: String, val title: String, val address: String, val shifts: List<ShiftView>)

data class EventView(val type: Int, val data: DataView)

fun EventView.targetParticipantsCount() =
    this.data.shifts.flatMap { it.places }.sumBy { it.targetParticipantsCount }


fun EventView.beginTime() =
    this.data.shifts.minBy { it.beginTime }!!.beginTime


fun EventView.endTime() =
    this.data.shifts.maxBy { it.endTime }!!.endTime


fun EventView.invited() =
    this.data.shifts.flatMap { it.places }.flatMap { it.invited }.map { it.user }


fun DataView.invited() =
    this.shifts.flatMap { it.places }.flatMap { it.invited }.map { it.user }
