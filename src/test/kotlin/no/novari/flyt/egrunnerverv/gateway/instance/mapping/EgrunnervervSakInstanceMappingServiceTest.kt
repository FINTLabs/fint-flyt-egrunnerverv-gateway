package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import no.novari.flyt.egrunnerverv.gateway.exception.ArchiveResourceNotFoundException
import no.novari.flyt.egrunnerverv.gateway.exception.NonMatchingEmailDomainWithOrgIdException
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakInstance
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakKlassering
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSaksPart
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
import org.mockito.Mockito.`when`
import java.util.UUID

class EgrunnervervSakInstanceMappingServiceTest {
    private lateinit var resourceRepository: ResourceRepository
    private lateinit var formattingUtilsService: FormattingUtilsService
    private lateinit var slackAlertService: SlackAlertService
    private lateinit var persistFile: (File) -> UUID

    @BeforeEach
    fun setUp() {
        resourceRepository = mock(ResourceRepository::class.java)
        formattingUtilsService = mock(FormattingUtilsService::class.java)
        slackAlertService = mock(SlackAlertService::class.java)
        persistFile = { _: File -> UUID.randomUUID() }

        lenient()
            .`when`(formattingUtilsService.formatKommunenavn(anyString()))
            .thenReturn("TestKommunenavn")

        lenient()
            .`when`(formattingUtilsService.formatEmail(anyString()))
            .thenAnswer { inv -> inv.getArgument(0) }

        lenient()
            .`when`(formattingUtilsService.extractEmailDomain(anyString()))
            .thenReturn("novari.no")
    }

    private fun createService(
        checkSaksansvarligEpost: Boolean = true,
        checkEmailDomain: Boolean = true,
        orgId: String = "novari.no",
    ): EgrunnervervSakInstanceMappingService =
        EgrunnervervSakInstanceMappingService(
            checkSaksansvarligEpost = checkSaksansvarligEpost,
            checkEmailDomain = checkEmailDomain,
            orgId = orgId,
            resourceRepository = resourceRepository,
            formattingUtilsService = formattingUtilsService,
            slackAlertService = slackAlertService,
        )

    private fun createInstance(): EgrunnervervSakInstance =
        EgrunnervervSakInstance(
            sysId = "sys1",
            table = "sak",
            knr = "1",
            gnr = "2",
            bnr = "3",
            fnr = "4",
            snr = "5",
            takstnummer = "6",
            tittel = "Tittel",
            saksansvarligEpost = "person@novari.no",
            eierforholdsnavn = "Eiernavn",
            eierforholdskode = "KODE",
            prosjektnr = "42",
            prosjektnavn = "Prosjekt",
            kommunenavn = "OSLO",
            adresse = "Gate 1",
            saksparter =
                listOf(
                    EgrunnervervSaksPart(
                        navn = "P1",
                        organisasjonsnummer = "999999999",
                        epost = "p1@novari.no",
                        telefon = "11111111",
                        postadresse = "Adr1",
                        postnummer = "1000",
                        poststed = "Oslo",
                    ),
                ),
            klasseringer =
                listOf(
                    EgrunnervervSakKlassering(
                        ordningsprinsipp = "OP",
                        ordningsverdi = "OV",
                        beskrivelse = "Beskrivelse",
                        sortering = "1",
                        untattOffentlighet = "false",
                    ),
                ),
        )

    @Test
    @DisplayName("Maps instance correctly when lookup succeeds")
    fun shouldMapInstance() {
        `when`(resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no"))
            .thenReturn("saksansvarligHref")

        val service = createService()
        val instance = createInstance()

        val result = service.map(123L, instance, persistFile)

        assertThat(result).isNotNull
        assertThat(result.valuePerKey)
            .containsEntry("sys_id", "sys1")
            .containsEntry("saksansvarlig", "saksansvarligHref")
            .containsEntry("kommunenavn", "TestKommunenavn")

        assertThat(result.objectCollectionPerKey)
            .containsKeys("saksparter", "klasseringer")

        assertThat(result.objectCollectionPerKey["saksparter"]).hasSize(1)
        assertThat(result.objectCollectionPerKey["klasseringer"]).hasSize(1)
    }

    @Test
    @DisplayName("Throws when email domain does not match orgId")
    fun shouldThrowWhenEmailDomainDoesNotMatch() {
        val service = createService()
        val instance = createInstance().copy(saksansvarligEpost = "person@wrong.no")

        `when`(formattingUtilsService.extractEmailDomain("person@wrong.no"))
            .thenReturn("wrong.no")

        assertThatThrownBy {
            service.map(123L, instance, persistFile)
        }.isInstanceOf(NonMatchingEmailDomainWithOrgIdException::class.java)
    }

    @Test
    @DisplayName("Throws when archive resource is missing")
    fun shouldThrowWhenArchiveResourceMissing() {
        val service = createService()
        val instance = createInstance()

        `when`(resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no"))
            .thenReturn(null)

        assertThatThrownBy {
            service.map(123L, instance, persistFile)
        }.isInstanceOf(ArchiveResourceNotFoundException::class.java)
    }

    @Test
    @DisplayName("Skips email domain validation when checkEmailDomain=false")
    fun shouldSkipEmailDomainCheck() {
        val service = createService(checkEmailDomain = false)
        val instance = createInstance().copy(saksansvarligEpost = "person@wrong.no")

        `when`(resourceRepository.getArkivressursHrefFromPersonEmail(anyString()))
            .thenReturn("href")

        val result = service.map(123L, instance, persistFile)

        assertThat(result).isNotNull
    }

    @Test
    @DisplayName("Skips archive lookup when checkSaksansvarligEpost=false")
    fun shouldSkipArchiveLookup() {
        val service = createService(checkSaksansvarligEpost = false)
        val instance = createInstance()

        val result = service.map(123L, instance, persistFile)

        assertThat(result).isNotNull
        assertThat(result.valuePerKey)
            .containsEntry("saksansvarlig", "")
    }
}
