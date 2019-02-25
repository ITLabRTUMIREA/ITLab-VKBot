package com.rtu.itlab.utils

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.io.File

class Config {
    companion object {
        var pathToConfFile: String? = null
    }

    val companion = Companion

    var config: com.typesafe.config.Config? = null
    private val logger = LoggerFactory.getLogger("com.rtu.itlab.utils.Config")

    private fun loadConfig() {
        config = when (pathToConfFile) {
            null -> ConfigFactory.load()
            else -> {
                val file = File(pathToConfFile)
                ConfigFactory.parseFile(file)
            }
        }
        if (config != null && !config!!.isEmpty) {
            logger.info("Config loaded!")
        } else {
            logger.error("Can't load config!")
        }
    }


    /**
     * if u want default path: src/main/resources/application.conf, then pathToConfFile must be null
     * @param pathToConfFile path to config file or null
     */
    fun updatePathToConfFile(pathToConfFile: String?) {
        this.companion.pathToConfFile = pathToConfFile
    }


    /**
     * Default constrictor if config located in the src/main/resources/application.conf
     */
    constructor() {
        logger.info("Loading config")
        loadConfig()
    }

    /**
     * Constructor, which can init config in path
     * @param pathToConfFile path to config file
     */
    constructor(pathToConfFile: String?) {
        logger.info("Loading config")
        this.companion.pathToConfFile = pathToConfFile
        loadConfig()

    }

    /**
     * Check path in config file. If the path is then return its value otherwise null
     * @param path path in config file
     * @return string value from config file or null
     */
    fun checkPath(path: String): String? =
        if (config != null && config!!.hasPath(path)) config!!.getString(path) else null


}