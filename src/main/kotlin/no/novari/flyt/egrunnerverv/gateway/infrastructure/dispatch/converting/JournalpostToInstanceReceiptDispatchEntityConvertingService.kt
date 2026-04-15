package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting

import no.novari.cache.FintCache
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.novari.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.fint.model.resource.felles.PersonResource
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil.getFirstLink
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.JournalpostReceipt
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

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
            getFirstLink(sakResource::getSaksansvarlig)
                ?.let { arkivressursResourceCache.getOptional(it).orElse(null) }
                ?.let { getFirstLink(it::getPersonalressurs) }
                ?.let { personalressursResourceCache.getOptional(it).orElse(null) }

        val saksansvarligPersonResource =
            saksansvarligPersonalressursResource
                ?.let { getFirstLink(it::getPerson) }
                ?.let { personResourceCache.getOptional(it).orElse(null) }

        val journalpostResource =
            sakResource.journalpost
                .firstOrNull { it.journalPostnummer == journalpostNummer }
                ?: throw IllegalArgumentException("No journalpost with journalpostNummer=$journalpostNummer")

        val administrativEnhetResource =
            getFirstLink(journalpostResource::getAdministrativEnhet)
                ?.let { administrativEnhetResourceCache.getOptional(it).orElse(null) }

        val journalStatusResource =
            getFirstLink(journalpostResource::getJournalstatus)
                ?.let { journalStatusResourceCache.getOptional(it).orElse(null) }

        val journalpostTypeResource =
            getFirstLink(journalpostResource::getJournalposttype)
                ?.let { journalpostTypeResourceCache.getOptional(it).orElse(null) }

        val skjermingResource = journalpostResource.skjerming

        val tilgangsrestriksjonResource =
            skjermingResource
                ?.let { getFirstLink(it::getTilgangsrestriksjon) }
                ?.let { tilgangsrestriksjonResourceCache.getOptional(it).orElse(null) }

        val skjermingshjemmelResource =
            skjermingResource
                ?.let { getFirstLink(it::getSkjermingshjemmel) }
                ?.let { skjermingshjemmelResourceCache.getOptional(it).orElse(null) }

        return JournalpostReceipt(
            journalpostnr = "${sakResource.mappeId.identifikatorverdi}-${journalpostResource.journalPostnummer}",
            tittel = journalpostResource.tittel,
            statusId = journalStatusResource?.systemId?.identifikatorverdi,
            tilgangskode = tilgangsrestriksjonResource?.kode,
            hjemmel = skjermingshjemmelResource?.systemId?.identifikatorverdi,
            dokumentdato = formatDate(journalpostResource.opprettetDato),
            dokumenttypeid = journalpostTypeResource?.systemId?.identifikatorverdi,
            saksansvarligbrukernavn = saksansvarligPersonalressursResource?.brukernavn?.identifikatorverdi,
            saksansvarlignavn = saksansvarligPersonResource?.let(::fullName),
            adminenhetkortnavn = administrativEnhetResource?.systemId?.identifikatorverdi,
            adminenhetnavn = administrativEnhetResource?.navn,
            dokumenttypenavn = null,
        )
    }

    private fun formatDate(date: Date?): String? =
        date
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDateTime()
            ?.format(
                DateTimeFormatter.ofPattern(
                    InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService.EGRUNNERVERV_DATETIME_FORMAT,
                ),
            )

    private fun fullName(person: PersonResource): String =
        listOfNotNull(
            person.navn.fornavn,
            person.navn.mellomnavn,
            person.navn.etternavn,
        ).joinToString(" ")
}
