package no.novari.flyt.egrunnerverv.gateway

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.sun.net.httpserver.HttpServer
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstance
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakInstance
import no.novari.flyt.gateway.instance.InstanceProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.net.InetSocketAddress
import java.time.Instant

/** Signed tokens through the app's real `JwtDecoder` and the filter chains of flyt-security-starter. */
@SpringBootTest(
    properties = [
        "fint.org-id=fintlabs.no",
        "novari.kafka.topic.org-id=fintlabs-no",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "fint.flyt.egrunnerverv.sso.client-id=egrunnerverv-client",
        "fint.flyt.authorization.sso.client-id=internal-client",
        "fint.flyt.authorization.sso.client-secret=test",
        "fint.sso.client-id=file-client",
        "fint.sso.client-secret=test",
        "novari.flyt.egrunnerverv.dispatch.client-id=service-now-client",
        "novari.flyt.egrunnerverv.dispatch.client-secret=test",
        "slack.webhook.url=",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
class EgrunnervervSecurityHttpTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var listeners: KafkaListenerEndpointRegistry

    @MockitoBean(name = "sakInstanceProcessor")
    private lateinit var sakInstanceProcessor: InstanceProcessor<EgrunnervervSakInstance>

    @MockitoBean(name = "journalpostInstanceProcessor")
    private lateinit var journalpostInstanceProcessor: InstanceProcessor<EgrunnervervJournalpostInstance>

    @Test
    fun `egrunnerverv client submits a sak instance`() {
        submit(token("egrunnerverv-client"), 202)
        verify(sakInstanceProcessor).processInstance(any<EgrunnervervSakInstance>())
    }

    @Test
    fun `internal client and another client cannot submit instances`() {
        submit(token("internal-client"), 401)
        submit(token("another-client"), 401)
        verifyNoInteractions(sakInstanceProcessor, journalpostInstanceProcessor)
    }

    @Test
    fun `invalid signature cannot submit instances`() {
        val otherKey = RSAKeyGenerator(2048).keyID(key.keyID).generate()
        submit(token("egrunnerverv-client", NimbusJwtEncoder(ImmutableJWKSet(JWKSet(otherKey)))), 401)
        verifyNoInteractions(sakInstanceProcessor)
    }

    @Test
    fun `listeners keep their topic names and the application group id`() {
        val containers = listeners.listenerContainers
        assertThat(containers.map { it.groupId }).containsOnly("fint-flyt-egrunnerverv-gateway")
        assertThat(containers.flatMap { it.containerProperties.topics!!.toList() }).containsExactlyInAnyOrder(
            "fintlabs-no.flyt.entity.arkiv-noark-administrativenhet",
            "fintlabs-no.flyt.entity.arkiv-noark-arkivressurs",
            "fintlabs-no.flyt.entity.arkiv-kodeverk-tilgangsrestriksjon",
            "fintlabs-no.flyt.entity.arkiv-kodeverk-skjermingshjemmel",
            "fintlabs-no.flyt.entity.arkiv-kodeverk-journalstatus",
            "fintlabs-no.flyt.entity.arkiv-kodeverk-journalposttype",
            "fintlabs-no.flyt.entity.administrasjon-personal-personalressurs",
            "fintlabs-no.flyt.entity.administrasjon-personal-person",
            "fintlabs-no.flyt.event.instance-dispatched",
        )
    }

    private fun submit(
        token: String,
        expectedStatus: Int,
    ) {
        mvc
            .perform(
                post("/api/egrunnerverv/instances/999/archive")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(SAK_INSTANCE),
            ).andExpect(status().`is`(expectedStatus))
    }

    companion object {
        private const val ISSUER = "https://idp.test/nam"
        private const val SAK_INSTANCE = """{"sys_id":"sys1","table":"table","knr":"1","gnr":"2","bnr":"3","fnr":"4",
            "snr":"5","takstnummer":"6","tittel":"Tittel","saksansvarligEpost":"test@novari.no",
            "eierforholdsnavn":"Eiernavn","eierforholdskode":"KODE","prosjektnr":"42","prosjektnavn":"Prosjekt",
            "kommunenavn":"OSLO","adresse":"Gate 1"}"""
        private val key = RSAKeyGenerator(2048).keyID("egrunnerverv-test").generate()
        private val encoder = NimbusJwtEncoder(ImmutableJWKSet(JWKSet(key)))
        private val jwks =
            HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
                createContext("/jwks") { exchange ->
                    val body = JWKSet(key.toPublicJWK()).toString().toByteArray()
                    exchange.responseHeaders.add("Content-Type", "application/json")
                    exchange.sendResponseHeaders(200, body.size.toLong())
                    exchange.responseBody.use { it.write(body) }
                }
                start()
            }

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { ISSUER }
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri") {
                "http://127.0.0.1:${jwks.address.port}/jwks"
            }
            registry.add("fint.database.url", postgres::getJdbcUrl)
            registry.add("fint.database.username", postgres::getUsername)
            registry.add("fint.database.password", postgres::getPassword)
            registry.add("spring.datasource.hikari.schema") { "public" }
            registry.add("spring.flyway.schemas") { "public" }
            registry.add("spring.flyway.default-schema") { "public" }
            registry.add("spring.flyway.placeholders.fint.database.username") { postgres.username }
        }

        private fun token(
            clientId: String,
            signer: NimbusJwtEncoder = encoder,
        ): String {
            val now = Instant.now()
            val claims =
                JwtClaimsSet
                    .builder()
                    .issuer(ISSUER)
                    .subject(clientId)
                    .audience(listOf(clientId))
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(300))
                    .claim("scope", "end-user-profile fint-api")
                    .build()
            val header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(key.keyID).build()
            return signer.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        }
    }
}
