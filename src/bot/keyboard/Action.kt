package bot.keyboard

import com.google.gson.Gson

class Action {

    private val type: String
    private val label: String
    private val payload: String

    constructor(
        type: String,
        label: String,
        payload: Payload
    ) {
        this.type = type
        this.label = label
        this.payload = Gson().toJson(payload).toString()
    }
}