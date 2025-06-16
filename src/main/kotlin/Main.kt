package me.grian

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import java.util.*
import kotlin.system.exitProcess
import kotlin.text.toByteArray

suspend fun main(args: Array<String>) {
    val selectorManager = SelectorManager(Dispatchers.IO)
    var socket = aSocket(selectorManager).tcp().connect(args[0], args[1].toInt())

    var readChannel = socket.openReadChannel()
    var writeChannel = socket.openWriteChannel(autoFlush = true)

    val sc = Scanner(System.`in`)
    val bytes = Buffer()

    while (sc.hasNextLine()) {
        val next = sc.nextLine()

        when {
            next == "q" -> {
                withContext(Dispatchers.IO) {
                    socket.close()
                    selectorManager.close()
                }
                exitProcess(0)
            }
            next == "w" -> {
                println("Sending: ${bytes.peek().readByteArray().toList()}")
                writeChannel.writePacket(bytes)
                writeChannel.flush()

                try {
                    withTimeoutOrNull(500) {
                        val response = readChannel.readByteArray(readChannel.availableForRead)
                        println("Received: ${response.toList()}")
                    } ?: println("No response received")
                } catch (e: Exception) {
                    println("Error reading response: ${e.message}")
                }

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
                bytes.writeByte(value)
            }
            next.startsWith("str") -> {
                val value = next.split(" ").drop(1).joinToString(" ")
                bytes.writeInt(value.length)
                bytes.writeFully(value.toByteArray())
            }
            next.startsWith("int") -> {
                val value = next.split(" ")[1].toInt()
                bytes.writeInt(value)
            }
        }
    }
}