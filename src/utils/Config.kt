package utils

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.io.File

class Config {

    companion object {
        var pathToConfFile: String? = null
        var config: com.typesafe.config.Config? = null
    }

    val companion = Companion

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun loadConfig() {
        logger.info("Loading config")
        config = when (pathToConfFile) {
            null -> ConfigFactory.load()
            else -> {
                val file = File(pathToConfFile)
                ConfigFactory.parseFile(file)
            }
        }
        if (config != null && !config!!.isEmpty) {
            logger.info("Config $pathToConfFile loaded")
        } else {
            logger.error("Can't load config $pathToConfFile")
        }
    }


    /**
     * if u want default path: resources/applicationSecure.conf, then pathToConfFile must be null
     * @param pathToConfFile path to config file or null
     */
    fun updatePathToConfFile(pathToConfFile: String?) {
        Companion.pathToConfFile = pathToConfFile
    }


    /**
     * Default constrictor if config located in the resources/secureInfo.conf
     */
    constructor() {
        if (pathToConfFile == null) pathToConfFile = "resources/secureInfo.conf"
    }

    /**
     * Constructor, which can init config in path
     * @param pathToConfFile path to config file
     */
    constructor(pathToConfFile: String?) {
        Companion.pathToConfFile = pathToConfFile
    }

    /**
     * Load info from config file. If the path is then and config found return this value otherwise null
     * @param path path in config file
     * @return string value from config file or null
     */
    fun loadPath(path: String): String? {
        return if (config != null && config!!.hasPath(path)) {
            logger.info("$path loaded from config")
            config!!.getString(path)
        } else {
            logger.error("Can't load $path from config")
            null
        }
    }

}