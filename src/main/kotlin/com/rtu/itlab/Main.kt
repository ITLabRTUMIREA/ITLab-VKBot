package com.rtu.itlab

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

private const val portArg = "-port"
private var port = 8080

fun main(args: Array<String>) {

    val portObtainedInArgs  = args.isNotEmpty() && args[0].startsWith(portArg)

    if(portObtainedInArgs)
        port = args[0].split("=").last().trim().toInt()

    embeddedServer(Netty, port, module = Application::main).start(wait = true)
}
