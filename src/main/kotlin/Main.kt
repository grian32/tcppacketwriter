package me.grian

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.netty.buffer.Unpooled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
    val selectorManager = SelectorManager(Dispatchers.IO)
    var socket = aSocket(selectorManager).tcp().connect(args[0], args[1].toInt())

    var writeChannel = socket.openWriteChannel(autoFlush = true)

    val sc = Scanner(System.`in`)
    val bytes = Unpooled.buffer()

    while (sc.hasNextLine()) {
        val next = sc.nextLine()

        when {
            next == "q" -> exitProcess(0)
            next == "w" -> {
                writeChannel.writeFully(bytes.nioBuffer())
                println("sent: ${bytes.nioBuffer()}")
                bytes.clear()
            }
            next == "rc" -> {
                withContext(Dispatchers.IO) {
                    socket.close()
                }
                socket = aSocket(selectorManager).tcp().connect(args[0], args[1].toInt())
                writeChannel = socket.openWriteChannel(autoFlush = true)
                bytes.clear()
            }
            next.startsWith("byte") -> {
                val value = next.split(" ")[1].removePrefix("0x").toByte()
                bytes.writeByte(value.toInt())
            }
            next.startsWith("str") -> {
                val value = next.split(" ").drop(1).joinToString(" ")
                bytes.writeInt(value.length)
                println(value.toByteArray().toList())
                bytes.writeBytes(value.toByteArray())
            }
        }
    }

    withContext(Dispatchers.IO) {
        socket.close()
        selectorManager.close()
    }
}