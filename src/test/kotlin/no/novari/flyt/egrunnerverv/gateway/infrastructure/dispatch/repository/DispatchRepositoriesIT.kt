package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.repository

import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceHeadersEntity
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceReceiptDispatchEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Testcontainers
class DispatchRepositoriesIT {
    @Autowired
    private lateinit var instanceHeadersRepository: InstanceHeadersRepository

    @Autowired
    private lateinit var instanceReceiptDispatchRepository: InstanceReceiptDispatchRepository

    @Test
    fun `flyway migrations run and repositories can persist entities`() {
        val header =
            instanceHeadersRepository.save(
                InstanceHeadersEntity(
                    sourceApplicationInstanceId = "id-1",
                    sourceApplicationIntegrationId = "sak",
                    archiveInstanceId = "ark-1",
                ),
            )

        assertThat(instanceHeadersRepository.findById(header.sourceApplicationInstanceId))
            .get()
            .extracting(InstanceHeadersEntity::archiveInstanceId)
            .isEqualTo("ark-1")

        val receipt =
            instanceReceiptDispatchRepository.save(
                InstanceReceiptDispatchEntity(
                    sourceApplicationInstanceId = "id-1",
                    uri = "http://example",
                    instanceReceipt = """{"ok":true}""",
                ),
            )

        assertThat(instanceReceiptDispatchRepository.findById(receipt.sourceApplicationInstanceId))
            .get()
            .extracting(InstanceReceiptDispatchEntity::uri)
            .isEqualTo("http://example")
    }

    private companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("egrunnerverv")
                .withUsername("test")
                .withPassword("test")

        @DynamicPropertySource
        @JvmStatic
        fun registerProperties(registry: DynamicPropertyRegistry) {
            // Avoid pulling in "included" profiles from application.yaml (e.g. flyt-postgres) which depend on external
            // placeholders. This test wires everything explicitly through Testcontainers.
            registry.add("spring.profiles.include") { "" }
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.datasource.hikari.schema") { "public" }
            // Ensure Flyway only runs this project's migrations (not any dependency-provided migrations on the classpath).
            registry.add("spring.flyway.locations") { "filesystem:src/main/resources/db/migration" }
            registry.add("spring.flyway.schemas") { "public" }
            registry.add("spring.flyway.default-schema") { "public" }
            // Defensive: if any migration uses this placeholder name, provide a value.
            registry.add("spring.flyway.placeholders.fint.database.username") { postgres.username }
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }
        }
    }
}
