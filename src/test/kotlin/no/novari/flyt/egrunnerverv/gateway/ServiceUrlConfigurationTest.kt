package no.novari.flyt.egrunnerverv.gateway

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.ClassPathResource

class ServiceUrlConfigurationTest {
    @Test
    fun `internal service URLs use the service context path, not the gateway context path`() {
        val environment = StandardEnvironment()
        environment.propertySources.addFirst(
            MapPropertySource(
                "overlay",
                mapOf(
                    "server.servlet.context-path" to "/beta",
                    "novari.flyt.egrunnerverv.service-context-path" to "/beta/afk-no",
                ),
            ),
        )
        YamlPropertySourceLoader()
            .load("application", ClassPathResource("application.yaml"))
            .forEach(environment.propertySources::addLast)

        assertThat(environment.getProperty("novari.flyt.intgr-conf-service-url"))
            .isEqualTo("http://fint-flyt-intgr-conf-service:8080/beta/afk-no")
        assertThat(environment.getProperty("novari.flyt.history-service-url"))
            .isEqualTo("http://fint-flyt-history-service:8080/beta/afk-no")
        assertThat(environment.getProperty("novari.flyt.archive-gateway-url"))
            .isEqualTo("http://fint-flyt-archive-gateway:8080/beta/afk-no")
    }
}
