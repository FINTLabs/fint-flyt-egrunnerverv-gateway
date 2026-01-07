package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import no.novari.flyt.egrunnerverv.gateway.exception.ArchiveResourceNotFoundException
import no.novari.flyt.egrunnerverv.gateway.exception.NonMatchingEmailDomainWithOrgIdException
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakInstance
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakKlassering
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSaksPart
import no.novari.flyt.egrunnerverv.gateway.slack.SlackAlertService
import no.novari.flyt.gateway.webinstance.InstanceMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EgrunnervervSakInstanceMappingService(
    @param:Value("\${novari.flyt.egrunnerverv.checkSaksansvarligEpost:true}")
    private val checkSaksansvarligEpost: Boolean,
    @param:Value("\${novari.flyt.egrunnerverv.checkEmailDomain:true}")
    private val checkEmailDomain: Boolean,
    @param:Value("\${fint.org-id}")
    private val orgId: String,
    private val resourceRepository: ResourceRepository,
    private val formattingUtilsService: FormattingUtilsService,
    private val slackAlertService: SlackAlertService,
) : InstanceMapper<EgrunnervervSakInstance> {
    override fun map(
        sourceApplicationId: Long,
        incomingInstance: EgrunnervervSakInstance,
        persistFile: (File) -> UUID,
    ): InstanceObject {
        val saksansvarligEpostFormatted =
            formattingUtilsService.formatEmail(incomingInstance.saksansvarligEpost)

        if (checkEmailDomain) {
            val domain = formattingUtilsService.extractEmailDomain(saksansvarligEpostFormatted)
            if (domain != orgId) {
                throw NonMatchingEmailDomainWithOrgIdException(domain, orgId)
            }
        }

        val saksansvarlig =
            if (checkSaksansvarligEpost) {
                resourceRepository
                    .getArkivressursHrefFromPersonEmail(saksansvarligEpostFormatted)
                    ?: throw ArchiveResourceNotFoundException(
                        saksansvarligEpostFormatted,
                        slackAlertService,
                    )
            } else {
                ""
            }

        val valuePerKey =
            mutableMapOf(
                "sys_id" to incomingInstance.sysId,
                "knr" to incomingInstance.knr,
                "gnr" to incomingInstance.gnr,
                "bnr" to incomingInstance.bnr,
                "fnr" to incomingInstance.fnr,
                "snr" to incomingInstance.snr,
                "takstnummer" to incomingInstance.takstnummer,
                "tittel" to incomingInstance.tittel,
                "saksansvarligEpost" to saksansvarligEpostFormatted,
                "saksansvarlig" to saksansvarlig,
                "eierforholdsnavn" to incomingInstance.eierforholdsnavn,
                "eierforholdskode" to incomingInstance.eierforholdskode,
                "prosjektnr" to incomingInstance.prosjektnr,
                "prosjektnavn" to incomingInstance.prosjektnavn,
                "kommunenavn" to
                    formattingUtilsService
                        .formatKommunenavn(incomingInstance.kommunenavn)
                        .orEmpty(),
                "adresse" to incomingInstance.adresse,
            )

        return InstanceObject(
            valuePerKey,
            mutableMapOf(
                "saksparter" to incomingInstance.saksparter.map(this::toInstanceObject),
                "klasseringer" to incomingInstance.klasseringer.map(this::toInstanceObject),
            ),
        )
    }

    private fun toInstanceObject(egrunnervervSaksPart: EgrunnervervSaksPart): InstanceObject =
        InstanceObject(
            mapOf(
                "navn" to egrunnervervSaksPart.navn,
                "organisasjonsnummer" to egrunnervervSaksPart.organisasjonsnummer,
                "epost" to egrunnervervSaksPart.epost,
                "telefon" to egrunnervervSaksPart.telefon,
                "postadresse" to egrunnervervSaksPart.postadresse,
                "postnummer" to egrunnervervSaksPart.postnummer,
                "poststed" to egrunnervervSaksPart.poststed,
            ),
            mutableMapOf(),
        )

    private fun toInstanceObject(egrunnervervSakKlassering: EgrunnervervSakKlassering): InstanceObject =
        InstanceObject(
            mapOf(
                "ordningsprinsipp" to egrunnervervSakKlassering.ordningsprinsipp,
                "ordningsverdi" to egrunnervervSakKlassering.ordningsverdi,
                "beskrivelse" to egrunnervervSakKlassering.beskrivelse,
                "sortering" to egrunnervervSakKlassering.sortering,
                "untattOffentlighet" to egrunnervervSakKlassering.untattOffentlighet,
            ),
            mutableMapOf(),
        )
}
