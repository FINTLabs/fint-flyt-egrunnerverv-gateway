package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.converting

import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.novari.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.fint.model.resource.felles.PersonResource
import no.novari.flyt.cache.FlytCache
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil.getFirstLink
import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.JournalpostReceipt
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@Service
class JournalpostToInstanceReceiptDispatchEntityConvertingService(
    private val administrativEnhetResourceCache: FlytCache<String, AdministrativEnhetResource>,
    private val arkivressursResourceCache: FlytCache<String, ArkivressursResource>,
    private val journalStatusResourceCache: FlytCache<String, JournalStatusResource>,
    private val journalpostTypeResourceCache: FlytCache<String, JournalpostTypeResource>,
    private val tilgangsrestriksjonResourceCache: FlytCache<String, TilgangsrestriksjonResource>,
    private val skjermingshjemmelResourceCache: FlytCache<String, SkjermingshjemmelResource>,
    private val personalressursResourceCache: FlytCache<String, PersonalressursResource>,
    private val personResourceCache: FlytCache<String, PersonResource>,
) {
    fun map(
        sakResource: SakResource,
        journalpostNummer: Long,
    ): JournalpostReceipt {
        val saksansvarligPersonalressursResource =
            getFirstLink(sakResource::getSaksansvarlig)
                ?.let { arkivressursResourceCache.getOrNull(it) }
                ?.let { getFirstLink(it::getPersonalressurs) }
                ?.let { personalressursResourceCache.getOrNull(it) }

        val saksansvarligPersonResource =
            saksansvarligPersonalressursResource
                ?.let { getFirstLink(it::getPerson) }
                ?.let { personResourceCache.getOrNull(it) }

        val journalpostResource =
            sakResource.journalpost
                .firstOrNull { it.journalPostnummer == journalpostNummer }
                ?: throw IllegalArgumentException("No journalpost with journalpostNummer=$journalpostNummer")

        val administrativEnhetResource =
            getFirstLink(journalpostResource::getAdministrativEnhet)
                ?.let { administrativEnhetResourceCache.getOrNull(it) }

        val journalStatusResource =
            getFirstLink(journalpostResource::getJournalstatus)
                ?.let { journalStatusResourceCache.getOrNull(it) }

        val journalpostTypeResource =
            getFirstLink(journalpostResource::getJournalposttype)
                ?.let { journalpostTypeResourceCache.getOrNull(it) }

        val skjermingResource = journalpostResource.skjerming

        val tilgangsrestriksjonResource =
            skjermingResource
                ?.let { getFirstLink(it::getTilgangsrestriksjon) }
                ?.let { tilgangsrestriksjonResourceCache.getOrNull(it) }

        val skjermingshjemmelResource =
            skjermingResource
                ?.let { getFirstLink(it::getSkjermingshjemmel) }
                ?.let { skjermingshjemmelResourceCache.getOrNull(it) }

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
