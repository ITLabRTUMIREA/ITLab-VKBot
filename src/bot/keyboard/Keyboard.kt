package bot.keyboard

import bot.BotCommands
import com.google.gson.Gson
import com.google.gson.JsonObject
import bot.keyboard.properties.Color
import com.vk.api.sdk.queries.messages.MessagesSendQuery
import com.vk.api.sdk.queries.messages.MessagesSendWithUserIdsQuery
import database.HibernateUtil
import database.schema.UserSettings
import org.slf4j.LoggerFactory
import workwithapi.RequestsToServerApi

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

private val logger = LoggerFactory.getLogger("bot.keyboard.Keyboard")

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

fun getKeyboardForCurrentPerson(
    requestsToServerApi: RequestsToServerApi,
    vkId: String,
    db: HibernateUtil
): KeyboardButtons {

    val keyboardButtons = KeyboardButtons()
    val id = requestsToServerApi.getIdByVkId(vkId)

    if (id != null) {

        val user = db.getEntityById(id, UserSettings())

        if (user != null) {
            keyboardButtons.createLine()
            keyboardButtons.createLine()
            //keyboardButtons.createLine()

            if (user.vkNotification)
                keyboardButtons.createButton(BotCommands.SubscribeVk.commandText, Color.GREEN)
            else
                keyboardButtons.createButton(BotCommands.SubscribeVk.commandText, Color.RED)

            if (user.newEventNotification)
                keyboardButtons.createButton(BotCommands.SubscribeNewEvent.commandText, Color.GREEN)
            else
                keyboardButtons.createButton(BotCommands.SubscribeNewEvent.commandText, Color.RED)

            if (user.changeEventNotification)
                keyboardButtons.createButton(BotCommands.SubscribeChangeEvent.commandText, Color.GREEN)
            else
                keyboardButtons.createButton(BotCommands.SubscribeChangeEvent.commandText, Color.RED)

            if (user.confirmEventNotification)
                keyboardButtons.createButton(BotCommands.SubscribeConfirmEvent.commandText, Color.GREEN)
            else
                keyboardButtons.createButton(BotCommands.SubscribeConfirmEvent.commandText, Color.RED)

            if (user.emailNotification)
                keyboardButtons.createButton(BotCommands.SubscribeEmail.commandText, Color.GREEN)
            else
                keyboardButtons.createButton(BotCommands.SubscribeEmail.commandText, Color.RED)

            keyboardButtons.addButtonToLine(0, 0)
            keyboardButtons.addButtonToLine(0, 4)
            keyboardButtons.addButtonToLine(1, 1)
            keyboardButtons.addButtonToLine(1, 2)
            keyboardButtons.addButtonToLine(1, 3)


        } else {
            logger.error("Can't get user in database by id")
        }
    } else {
        logger.error("Can't get user in database by id")
    }

    return keyboardButtons
}
