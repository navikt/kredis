package no.nav.aap.redis

import java.io.BufferedOutputStream
import java.net.Socket

private const val CR = '\r'.code
private const val LF = '\n'.code
private val CRLF = byteArrayOf(LF.toByte(), CR.toByte())

class Encoder(socket: Socket) {
    private val stream = BufferedOutputStream(socket.getOutputStream())
    /**
     * Redis [documentation](https://redis.io/docs/reference/protocol-spec/#arrays)
     */
    fun write(list: List<*>) {
        stream.write('*'.toByteArray())
        stream.write(list.size.toByteArray())
        stream.write(CRLF)
        list.forEach {
            when (it) {
                is ByteArray -> write(it)
                is String -> write(it.encodeToByteArray())
                is Long -> write(it)
                is Int -> write(it.toLong())
                is List<*> -> write(it)
                else -> error("Unsupported type: ${it?.javaClass?.canonicalName}")
            }
        }
    }

    /**
     * Redis [documentation](https://redis.io/docs/reference/protocol-spec/#bulk-strings)
     */
    private fun write(value: ByteArray) {
        stream.write('$'.toByteArray())
        stream.write(value.size.toByteArray())
        stream.write(CRLF)
        stream.write(value)
        stream.write(CRLF)
    }

    /**
     * Redis [documentation](https://redis.io/docs/reference/protocol-spec/#integers)
     */
    private fun write(value: Long) {
        stream.write(':'.toByteArray())
        stream.write(value.toByteArray())
        stream.write(CRLF)
    }

    fun flush() = stream.flush()
    fun close() = stream.close()
}

internal fun Char.toByteArray() = byteArrayOf(code.toByte())
internal fun Number.toByteArray(size: Int = 4) = ByteArray(size) { i -> (toLong() shr (i * 8)).toByte() }
