package com.rtu.itlab

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

//Server port
private const val SPORTARG = "-sp"
private var sPort = 8080

fun main(args: Array<String>) {

    val portObtainedInArgs  = args.isNotEmpty() && args[0].startsWith(SPORTARG)

    if(portObtainedInArgs)
        sPort = args[0].split("=").last().trim().toInt()

    embeddedServer(Netty, sPort, module = Application::main).start(wait = true)
}
