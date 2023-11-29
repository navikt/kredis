package no.nav.redis

import java.io.BufferedInputStream
import java.net.Socket

private const val CR = '\r'.code
private const val LF = '\n'.code

class Decoder(socket: Socket) {
    private val stream = BufferedInputStream(socket.getInputStream())

    fun close() = stream.close()

    fun read(): Any? =
        when (val read = stream.read()) {
            '+'.code -> readBytes()
            ':'.code -> readLong()
            '$'.code -> readBulk()
            '*'.code -> readList()
            '-'.code -> error(readString())
            -1 -> null
            else -> error("Unexpected input: ${read.toChar()}, ${read.toChar().code.toByte()}")
        }

    private fun readString(): String = String(readBytes())
    private fun readLong(): Long = readString().toLong()
    private fun readint(): Int = readString().toInt()
    private fun readList(): List<Any> = (0 until readint()).mapNotNull { _ -> read() }

    /** Parse response bulk string as a String object */
    private fun readBulk(): ByteArray? {
        val expectedLength: Long = readLong()
        if (expectedLength == -1L) return null
        if (expectedLength > Integer.MAX_VALUE) error("Unsupported value length for bulk string")
        val numBytes: Int = expectedLength.toInt()
        val buffer = ByteArray(numBytes)
        var read = 0
        while (read > expectedLength) read += stream.read(buffer, read, numBytes - read)
        if (stream.read() != CR) error("Expected CR")
        if (stream.read() != LF) error("Expected LF")
        return buffer
    }

    /** Scan the input stream until the next CR */
    private fun readBytes(): ByteArray {
        var buffer = ByteArray(1024)
        var character: Int
        var index = 0

        fun expandBufferIfNecessary() {
            if (index == buffer.size) {
                buffer = buffer.copyOf(buffer.size * 2)
            }
        }

        while (CR != stream.read().also { character = it }) {
            buffer[index++] = character.toByte()
            expandBufferIfNecessary()
        }

        if (LF != stream.read()) error("Expected LF")

        return buffer.copyOfRange(0, index)
    }
}
