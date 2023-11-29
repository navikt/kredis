package no.nav.redis

import java.net.Socket
import java.net.URI

data class RedisConfig(
    val uri: URI,
    val username: String,
    val password: String,
)

open class Redis(private val config: RedisConfig) {
    internal open fun connect(): Managed = ManagedImpl().also {
        it.call("HELLO", 3)
        it.call("AUTH", config.username, config.password)
    }

    operator fun set(key: String, value: ByteArray): Unit = connect().use {
        it.call("SET", key, value)
    }

    operator fun get(key: String): ByteArray? = connect().use {
        it.call("GET", key) as ByteArray?
    }

    fun expire(key: String, seconds: Long): Unit = connect().use {
        it.call("EXPIRE", key, seconds)
    }

    fun del(key: String): Unit = connect().use {
        it.call("DEL", key)
    }

    fun ready(): Boolean = connect().use {
        val ping = (it.call("PING") as? ByteArray)?.let(::String)
        return ping == "PONG"
    }

    interface Managed : AutoCloseable {
        fun call(vararg args: Any): Any?
    }

    inner class ManagedImpl : Managed, AutoCloseable {
        private val socket = Socket(config.uri.host, config.uri.port).apply {
            tcpNoDelay = true
            keepAlive = true
        }
        private val encoder = Encoder(socket)
        private val decoder = Decoder(socket)

        // See [docs](https://redis.io/commands)
        override fun call(vararg args: Any): Any? {
            encoder.write(args.toList()).also { encoder.close() }
//            writer.flush()
            return decoder.read().also { decoder.close() }
        }

        override fun close() {
            call("QUIT")
            socket.close()
        }
    }
}
