package bot

/**
 * A class that contains commands that a bot can execute
 */
enum class BotCommands(val commandText: String) {
    UnSubscribeVk("отписаться от vk рассылки"),
    UnSubscribeEmail("отписаться от email рассылки"),
    SubscribeEmail("подписаться на email рассылку"),
    SubscribeVk("подписаться на vk рассылку"),
    DeleteFromNotifyCenter("отвязать мои аккаунты от рассылок"),
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