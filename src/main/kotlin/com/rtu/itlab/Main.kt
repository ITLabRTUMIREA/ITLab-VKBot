package com.rtu.itlab
//Теперь чисто ветка develop, не будем объединять с master пока не решимся
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

const val PORT = 8080

private const val portArg = "-port"
private var port = 8878

fun main(args: Array<String>) {

    val portObtainedInArgs  = args.isNotEmpty() && args[0].startsWith(portArg)

    if(portObtainedInArgs)
        port = args[0].split("=").last().trim().toInt()

    embeddedServer(Netty, PORT, module = Application::main).start(wait = true)
}
