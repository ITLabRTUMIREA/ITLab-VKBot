package bot.keyboard.properties

/**
 * Class with color of button
 * @param type - type of color
 */
enum class Color(val type: String) {
    BLUE("primary"),
    WHITE("default"),
    RED("negative"),
    GREEN("positive")
}