package no.nav.aap.redis

import org.junit.jupiter.api.Assertions.assertEquals
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.net.URI

class RedisMock : AutoCloseable {
    // TODO: testcontainer or protocol impl not working
    private val server = GenericContainer(DockerImageName.parse("redis:7.0")).apply {
        addExposedPort(6379)
        start()
    }

    fun getURI(): URI = URI("http://${server.host}:${server.firstMappedPort}")
    override fun close() = server.close()
}

class RedisTest {
    //@Test
    fun test() {
        RedisMock().use { mock ->
            val redis = Redis(RedisConfig(mock.getURI(), "", ""))
            redis["a"] = "b".toByteArray()
            assertEquals("b", String(redis["a"]!!))
        }
    }
}