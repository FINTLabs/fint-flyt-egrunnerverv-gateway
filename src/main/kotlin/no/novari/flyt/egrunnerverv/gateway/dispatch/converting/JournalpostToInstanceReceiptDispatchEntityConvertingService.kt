package no.novari.flyt.egrunnerverv.gateway.dispatch.converting

import no.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.fint.model.resource.arkiv.noark.ArkivressursResource
import no.fint.model.resource.arkiv.noark.JournalpostResource
import no.fint.model.resource.arkiv.noark.SakResource
import no.fint.model.resource.arkiv.noark.SkjermingResource
import no.fint.model.resource.felles.PersonResource
import no.novari.cache.FintCache
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil.getOptionalFirstLink
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.JournalpostReceipt
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Objects
import java.util.stream.Collectors
import java.util.stream.Stream

@Service
class JournalpostToInstanceReceiptDispatchEntityConvertingService(
    private val administrativEnhetResourceCache: FintCache<String, AdministrativEnhetResource>,
    private val arkivressursResourceCache: FintCache<String, ArkivressursResource>,
    private val journalStatusResourceCache: FintCache<String, JournalStatusResource>,
    private val journalpostTypeResourceCache: FintCache<String, JournalpostTypeResource>,
    private val tilgangsrestriksjonResourceCache: FintCache<String, TilgangsrestriksjonResource>,
    private val skjermingshjemmelResourceCache: FintCache<String, SkjermingshjemmelResource>,
    private val personalressursResourceCache: FintCache<String, PersonalressursResource>,
    private val personResourceCache: FintCache<String, PersonResource>,
) {
    fun map(
        sakResource: SakResource,
        journalpostNummer: Long,
    ): JournalpostReceipt {
        val saksansvarligPersonalressursResource =
            getOptionalFirstLink(sakResource::getSaksansvarlig)
                ?.let(arkivressursResourceCache::getOptional)
                ?.orElse(null)
                ?.let { arkivressurs ->
                    getOptionalFirstLink(arkivressurs::getPersonalressurs)
                        ?.let(personalressursResourceCache::getOptional)
                        ?.orElse(null)
                }

        val saksansvarligPersonResource =
            saksansvarligPersonalressursResource
                ?.let { personalressursResource ->
                    getOptionalFirstLink(personalressursResource::getPerson)
                        ?.let(personResourceCache::getOptional)
                        ?.orElse(null)
                }

        val journalpostResource =
            sakResource.journalpost
                .stream()
                .filter { journalpost: JournalpostResource ->
                    Objects.equals(journalpost.journalPostnummer, journalpostNummer)
                }.findFirst()
                .orElseThrow {
                    IllegalArgumentException("No journalpost with journalpostNummer=$journalpostNummer")
                }

        val administrativEnhetResource =
            getOptionalFirstLink(journalpostResource::getAdministrativEnhet)
                ?.let(administrativEnhetResourceCache::getOptional)
                ?.orElse(null)

        val journalStatusResource =
            getOptionalFirstLink(journalpostResource::getJournalstatus)
                ?.let(journalStatusResourceCache::getOptional)
                ?.orElse(null)

        val journalpostTypeResource =
            getOptionalFirstLink(journalpostResource::getJournalposttype)
                ?.let(journalpostTypeResourceCache::getOptional)
                ?.orElse(null)

        val skjermingResource: SkjermingResource? = journalpostResource.skjerming

        val tilgangsrestriksjonResource =
            skjermingResource
                ?.let { sr ->
                    getOptionalFirstLink(sr::getTilgangsrestriksjon)
                        ?.let(tilgangsrestriksjonResourceCache::getOptional)
                        ?.orElse(null)
                }

        val skjermingshjemmelResource =
            skjermingResource
                ?.let { sr ->
                    getOptionalFirstLink(sr::getSkjermingshjemmel)
                        ?.let(skjermingshjemmelResourceCache::getOptional)
                        ?.orElse(null)
                }

        var receipt =
            JournalpostReceipt(
                journalpostnr = "${sakResource.mappeId.identifikatorverdi}-${journalpostResource.journalPostnummer}",
                tittel = journalpostResource.tittel,
                statusId = null,
                tilgangskode = null,
                hjemmel = null,
                dokumentdato = formatDate(journalpostResource.opprettetDato),
                dokumenttypeid = null,
                dokumenttypenavn = null,
                saksansvarligbrukernavn = null,
                saksansvarlignavn = null,
                adminenhetkortnavn = null,
                adminenhetnavn = null,
            )

        journalStatusResource
            ?.systemId
            ?.identifikatorverdi
            ?.let { statusId -> receipt = receipt.copy(statusId = statusId) }

        tilgangsrestriksjonResource
            ?.kode
            ?.let { tilgangskode -> receipt = receipt.copy(tilgangskode = tilgangskode) }

        skjermingshjemmelResource
            ?.systemId
            ?.identifikatorverdi
            ?.let { hjemmel -> receipt = receipt.copy(hjemmel = hjemmel) }

        journalpostTypeResource
            ?.systemId
            ?.identifikatorverdi
            ?.let { dokumenttypeid -> receipt = receipt.copy(dokumenttypeid = dokumenttypeid) }

        saksansvarligPersonalressursResource
            ?.brukernavn
            ?.identifikatorverdi
            ?.let { brukernavn -> receipt = receipt.copy(saksansvarligbrukernavn = brukernavn) }

        saksansvarligPersonResource
            ?.let(this::fullName)
            ?.let { navn -> receipt = receipt.copy(saksansvarlignavn = navn) }

        administrativEnhetResource
            ?.systemId
            ?.identifikatorverdi
            ?.let { kortnavn -> receipt = receipt.copy(adminenhetkortnavn = kortnavn) }

        administrativEnhetResource
            ?.navn
            ?.let { navn -> receipt = receipt.copy(adminenhetnavn = navn) }

        return receipt
    }

    private fun formatDate(date: Date?): String? =
        date
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDateTime()
            ?.format(DateTimeFormatter.ofPattern(EGRUNNERVERV_DATETIME_FORMAT))

    private fun fullName(person: PersonResource): String =
        Stream
            .of(
                person.navn.fornavn,
                person.navn.mellomnavn,
                person.navn.etternavn,
            ).filter(Objects::nonNull)
            .collect(Collectors.joining(" "))

    companion object {
        private const val EGRUNNERVERV_DATETIME_FORMAT =
            InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService.EGRUNNERVERV_DATETIME_FORMAT
    }
}
