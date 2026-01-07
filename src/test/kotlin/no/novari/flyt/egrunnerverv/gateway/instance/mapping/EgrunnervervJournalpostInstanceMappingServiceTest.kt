package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import no.novari.flyt.egrunnerverv.gateway.exception.ArchiveResourceNotFoundException
import no.novari.flyt.egrunnerverv.gateway.exception.NonMatchingEmailDomainWithOrgIdException
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostDocument
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstance
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstanceBody
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostReceiver
import no.novari.flyt.egrunnerverv.gateway.slack.SlackAlertService
import no.novari.flyt.gateway.webinstance.model.File
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class EgrunnervervJournalpostInstanceMappingServiceTest {
    private lateinit var resourceRepository: ResourceRepository
    private lateinit var formattingUtilsService: FormattingUtilsService
    private lateinit var slackAlertService: SlackAlertService
    private lateinit var persistFile: (File) -> UUID
    private lateinit var persistFileCallCount: AtomicInteger

    @BeforeEach
    fun setUp() {
        resourceRepository = mock(ResourceRepository::class.java)
        formattingUtilsService = mock(FormattingUtilsService::class.java)
        slackAlertService = mock(SlackAlertService::class.java)
        persistFileCallCount = AtomicInteger(0)
        persistFile = { _: File ->
            persistFileCallCount.incrementAndGet()
            UUID.randomUUID()
        }

        lenient()
            .`when`(formattingUtilsService.formatEmail(anyString()))
            .thenAnswer { inv -> inv.getArgument(0) }

        lenient()
            .`when`(formattingUtilsService.extractEmailDomain(anyString()))
            .thenReturn("novari.no")

        lenient()
            .`when`(formattingUtilsService.formatKommunenavn(anyString()))
            .thenReturn("FormattedKommune")
    }

    private fun createService(
        checkSaksbehandler: Boolean = true,
        checkEmailDomain: Boolean = true,
        orgId: String = "novari.no",
    ): EgrunnervervJournalpostInstanceMappingService =
        EgrunnervervJournalpostInstanceMappingService(
            checkSaksbehandler = checkSaksbehandler,
            checkEmailDomain = checkEmailDomain,
            orgId = orgId,
            resourceRepository = resourceRepository,
            formattingUtilsService = formattingUtilsService,
            slackAlertService = slackAlertService,
        )

    private fun createInstance(saksbehandlerEpost: String = "person@novari.no"): EgrunnervervJournalpostInstance {
        val hoveddok =
            EgrunnervervJournalpostDocument(
                tittel = "Hoveddok",
                filnavn = "hoved.pdf",
                dokumentBase64 = "AAAA",
                hoveddokument = true,
            )

        val vedlegg =
            EgrunnervervJournalpostDocument(
                tittel = "Vedlegg1",
                filnavn = "vedlegg1.pdf",
                dokumentBase64 = "BBBB",
                hoveddokument = false,
            )

        val receiver =
            EgrunnervervJournalpostReceiver(
                navn = "TestNavn",
                organisasjonsnummer = "123456789",
                epost = "test@novari.no",
                telefon = "11111111",
                postadresse = "Gate 1",
                postnummer = "1000",
                poststed = "Oslo",
            )

        val body =
            EgrunnervervJournalpostInstanceBody(
                sysId = "SYS123",
                table = "journalpost",
                tittel = "Sakstittel",
                dokumentNavn = "Doknavn",
                dokumentDato = "2020-01-01",
                forsendelsesMate = "epost",
                kommunenavn = "OSLO",
                knr = "1",
                gnr = "2",
                bnr = "3",
                fnr = "4",
                snr = "5",
                eierforhold = null,
                id = "ID123",
                maltittel = "Mal",
                prosjektnavn = "Prosjekt",
                saksbehandlerEpost = saksbehandlerEpost,
                dokumenter = listOf(hoveddok, vedlegg),
                mottakere = listOf(receiver),
            )

        return EgrunnervervJournalpostInstance(
            saksnummer = "SAK123",
            egrunnervervJournalpostInstanceBody = body,
        )
    }

    @Test
    @DisplayName("Maps full instance successfully")
    fun testHappyPathMapping() {
        `when`(resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no"))
            .thenReturn("href123")

        val service = createService()
        val instance = createInstance()

        val result = service.map(10L, instance, persistFile)

        assertThat(result).isNotNull
        assertThat(result.valuePerKey)
            .containsEntry("saksnummer", "SAK123")
            .containsEntry("kommunenavn", "FormattedKommune")
            .containsEntry("saksbehandler", "href123")

        assertThat(result.objectCollectionPerKey["vedlegg"]).hasSize(1)
        assertThat(result.objectCollectionPerKey["mottakere"]).hasSize(1)

        assertThat(persistFileCallCount.get()).isEqualTo(2)
    }

    @Test
    @DisplayName("Throws when email domain is wrong")
    fun testWrongEmailDomain() {
        `when`(formattingUtilsService.extractEmailDomain("person@wrong.no"))
            .thenReturn("wrong.no")

        val service = createService()
        val instance = createInstance("person@wrong.no")

        assertThatThrownBy {
            service.map(10L, instance, persistFile)
        }.isInstanceOf(NonMatchingEmailDomainWithOrgIdException::class.java)

        verify(resourceRepository, never()).getArkivressursHrefFromPersonEmail(anyString())
    }

    @Test
    @DisplayName("Throws when archive resource is missing")
    fun testArchiveResourceMissing() {
        `when`(resourceRepository.getArkivressursHrefFromPersonEmail(anyString()))
            .thenReturn(null)

        val service = createService()
        val instance = createInstance()

        assertThatThrownBy {
            service.map(10L, instance, persistFile)
        }.isInstanceOf(ArchiveResourceNotFoundException::class.java)
    }

    @Test
    @DisplayName("Skips domain check when checkEmailDomain=false")
    fun testSkipDomainCheck() {
        `when`(resourceRepository.getArkivressursHrefFromPersonEmail(anyString()))
            .thenReturn("href123")

        val service = createService(checkEmailDomain = false)
        val instance = createInstance("person@other.no")

        val result = service.map(1L, instance, persistFile)

        assertThat(result).isNotNull
    }

    @Test
    @DisplayName("Skips saksbehandler lookup when checkSaksbehandler=false")
    fun testSkipSaksbehandlerLookup() {
        val service = createService(checkSaksbehandler = false)
        val instance = createInstance()

        val result = service.map(1L, instance, persistFile)

        assertThat(result).isNotNull
        assertThat(result.valuePerKey)
            .containsEntry("saksbehandler", "")
    }
}
