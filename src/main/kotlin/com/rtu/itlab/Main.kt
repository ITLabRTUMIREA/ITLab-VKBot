package com.rtu.itlab

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

//Server port
private const val SPORTARG = "-p"
private var sPort = 8080

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("com.rtu.itlab.Server")

    val portObtainedInArgs = args.isNotEmpty() && args[0].startsWith(SPORTARG)

    if (portObtainedInArgs)
        sPort = args[0].split("=").last().trim().toInt()

    logger.info("Starting server at port $sPort")

    embeddedServer(Netty, sPort, module = Application::main).start(wait = true)
}
