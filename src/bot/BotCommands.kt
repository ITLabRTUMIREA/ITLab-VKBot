package bot

/**
 * A class that contains commands that a bot can execute
 */
enum class BotCommands(val commandText: String) {
    SubscribeVk("Vk рассылка"),
    SubscribeEmail("Email рассылка"),
    SubscribeNewEvent("Создание события"),
    SubscribeChangeEvent("Изменение события"),
    SubscribeConfirmEvent("Подтверждение участия"),
    DeleteFromNotifyCenter("отвязать мой аккаунт от рассылок"),
    Help("/help");

    companion object {
        fun getEnumClassByCommandText(commandText: String): BotCommands? {
            var result: BotCommands? = null
            for (botCommand in BotCommands.values()) {
                if (botCommand.commandText == commandText.toLowerCase()) {
                    result = botCommand
                    break
                }
            }
            return result
        }
    }

}