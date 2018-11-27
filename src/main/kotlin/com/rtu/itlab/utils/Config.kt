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
    val logger = LoggerFactory.getLogger("com.rtu.itlab.utils.Config")

    fun loadConfig() {
        config = when (pathToConfFile) {
            null -> ConfigFactory.load()
            else -> {
                val file = File(pathToConfFile)
                ConfigFactory.parseFile(file)
            }
        }
        if (config != null || !config!!.isEmpty) {
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

}