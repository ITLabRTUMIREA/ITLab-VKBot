package com.rtu.itlab.bot.keyboard

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rtu.itlab.bot.BotCommands
import com.rtu.itlab.bot.keyboard.properties.Color
import com.rtu.itlab.database.DBClient
import com.vk.api.sdk.queries.messages.MessagesSendQuery
import com.vk.api.sdk.queries.messages.MessagesSendWithUserIdsQuery
import org.slf4j.LoggerFactory

fun MessagesSendWithUserIdsQuery.keyboard(value: String): MessagesSendWithUserIdsQuery {
    return this.unsafeParam("keyboard", value) as MessagesSendWithUserIdsQuery
}

fun MessagesSendQuery.keyboard(value: String): MessagesSendQuery {
    return this.unsafeParam("keyboard", value) as MessagesSendQuery
}

/**
 * Keyboard class
 */
data class Keyboard(val one_time: Boolean, val buttons: List<List<Button>>)

private val logger = LoggerFactory.getLogger("com.rtu.itlab.bot.keyboard")

/**
 * Buttons class for Keyboard
 */
class KeyboardButtons {
    private val buttons = mutableListOf<Button>()
    val lines = mutableListOf<List<Button>>()

    /**
     * Create line for buttons
     */
    fun createLine() {
        lines.add(mutableListOf())
    }

    /**
     * Adding Button to line
     * @param numberOfButton index of Button in List of buttons
     * @param numberOfLine line number to which we want to add a button
     */
    fun addButtonToLine(numberOfLine: Int, numberOfButton: Int) {
        if (numberOfLine <= lines.size - 1) {

            val currentLine = lines[numberOfLine].toMutableList()

            if (numberOfButton <= buttons.size - 1) {
                currentLine.add(buttons[numberOfButton])
                lines[numberOfLine] = currentLine
            } else {
                logger.error("Invalid number of button!")
            }
        } else {
            logger.error("Invalid number of line!")
        }

    }

    /**
     * Creating new button
     * @param label text on button
     * @param color color of button
     */
    fun createButton(label: String, color: Color) {
        buttons.add(Button(Action("text", label, Payload(buttons.size.toString())), color))
    }

    /**
     * Creating new button
     * @param label text on button
     * @param color color of button
     * @param type type of button
     */
    fun createButton(label: String, color: Color, type: String) {
        buttons.add(Button(Action(type, label, Payload(buttons.size.toString())), color))
    }

    /**
     * Getting Keyboard class json
     * @param one_time times to show keyboard
     * @return keyboard json view
     */
    fun getKeyboardJson(one_time: Boolean = false): JsonObject {
        val jsonString = Gson().toJson(Keyboard(one_time, lines))
        return Gson().fromJson(jsonString, JsonObject::class.java)
    }

}

fun getKeyboardForCurrentPerson(vkId: Int, db: DBClient): KeyboardButtons {
    val keyboardButtons = KeyboardButtons()
    val userNotifications = db.getUserNotifications(vkId)

    if (userNotifications.get("statusCode").asInt == 1) {
        keyboardButtons.createLine()
        //keyboardButtons.createLine()
        //keyboardButtons.createLine()

        if (userNotifications.get("vkNotice").asBoolean)
            keyboardButtons.createButton(BotCommands.UnSubscribeVk.commandText, Color.RED)
        else
            keyboardButtons.createButton(BotCommands.SubscribeVk.commandText, Color.GREEN)

        if (userNotifications.get("emailNotice").asBoolean)
            keyboardButtons.createButton(BotCommands.UnSubscribeEmail.commandText, Color.RED)
        else
            keyboardButtons.createButton(BotCommands.SubscribeEmail.commandText, Color.GREEN)

        if (userNotifications.get("phoneNotice").asBoolean)
            keyboardButtons.createButton(BotCommands.UnSubscribePhone.commandText, Color.RED)
        else
            keyboardButtons.createButton(BotCommands.SubscribePhone.commandText, Color.GREEN)


        keyboardButtons.addButtonToLine(0, 0)
        //keyboardButtons.addButtonToLine(1, 1)
        //keyboardButtons.addButtonToLine(2, 2)
    }

    return keyboardButtons
}
