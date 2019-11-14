package responses.event

enum class NotifyType(val type: Int) {
    EventNew(0),
    EventChange(1),
    EventConfirm(2)
}