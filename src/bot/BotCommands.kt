package bot

/**
 * A class that contains commands that a bot can execute
 */
enum class BotCommands(val commandText: String) {
    SubscribeVk("Vk рассылка"),
    SubscribeEmail("Email рассылка"),
    SubscribeNewEvent("Создание события или &#10133;"),
    SubscribeChangeEvent("Изменение события или &#10135;"),
    SubscribeConfirmEvent("Подтверждение участия или &#10004;"),
    DeleteFromNotifyCenter("отвязать мой аккаунт от рассылок"),
    Help("/help");

    companion object {
        fun getEnumClassByCommandText(commandText: String): BotCommands? {
            var result: BotCommands? = null
            for (botCommand in values()) {
                if (botCommand.commandText.toLowerCase().split(" или")[0] == commandText.toLowerCase()) {
                    result = botCommand
                    break
                }
            }
            if (result == null && commandText.toCharArray().isNotEmpty()) {
                result = when (commandText.toCharArray()[0].toInt()) {
                    10133 -> SubscribeNewEvent
                    10135 -> SubscribeChangeEvent
                    10004 -> SubscribeConfirmEvent
                    else -> null
                }
            }

            return result
        }
    }

}