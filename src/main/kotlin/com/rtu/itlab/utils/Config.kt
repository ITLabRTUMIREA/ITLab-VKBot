package com.rtu.itlab.utils

import com.typesafe.config.ConfigFactory
import java.io.File

class Config {

    companion object {
        var pathToConfFile: String? = null
        var config: com.typesafe.config.Config? = null

        fun updateConfig() {
            config = when (pathToConfFile) {
                null -> ConfigFactory.load()
                else -> {
                    val file = File(pathToConfFile)
                    ConfigFactory.parseFile(file)
                }
            }
        }

        /**
         * if u want default path: src/main/resources/application.conf, then pathToConfFile must be null
         * @param pathToConfFile path to config file or null
         */
        fun updatePathToConfFile(pathToConfFile: String?) {
            this.pathToConfFile = pathToConfFile
        }

    }

    var companion = Companion

    /**
     * Default constrictor if config located in the src/main/resources/application.conf
     */
    constructor() {
        if (config == null)
            updateConfig()
    }

    /**
     * Constructor, which can init config in path
     * @param pathToConfFile path to config file
     */
    constructor(pathToConfFile: String?) {
        if (config == null) {
            this.companion.pathToConfFile = pathToConfFile
            updateConfig()
        }
    }

}