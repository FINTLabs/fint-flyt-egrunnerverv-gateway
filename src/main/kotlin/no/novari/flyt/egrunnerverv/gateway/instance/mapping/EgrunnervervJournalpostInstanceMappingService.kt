package no.novari.flyt.egrunnerverv.gateway.instance.mapping

import no.novari.flyt.egrunnerverv.gateway.exception.ArchiveResourceNotFoundException
import no.novari.flyt.egrunnerverv.gateway.exception.NonMatchingEmailDomainWithOrgIdException
import no.novari.flyt.egrunnerverv.gateway.instance.ResourceRepository
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostDocument
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstance
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostReceiver
import no.novari.flyt.egrunnerverv.gateway.slack.SlackAlertService
import no.novari.flyt.gateway.webinstance.InstanceMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EgrunnervervJournalpostInstanceMappingService(
    @param:Value("\${novari.flyt.egrunnerverv.checkSaksbehandler:true}")
    private val checkSaksbehandler: Boolean,
    @param:Value("\${novari.flyt.egrunnerverv.checkEmailDomain:true}")
    private val checkEmailDomain: Boolean,
    @param:Value("\${fint.org-id}")
    private val orgId: String,
    private val resourceRepository: ResourceRepository,
    private val formattingUtilsService: FormattingUtilsService,
    private val slackAlertService: SlackAlertService,
) : InstanceMapper<EgrunnervervJournalpostInstance> {
    override fun map(
        sourceApplicationId: Long,
        incomingInstance: EgrunnervervJournalpostInstance,
        persistFile: (File) -> UUID,
    ): InstanceObject {
        val body = incomingInstance.egrunnervervJournalpostInstanceBody
        val saksbehandlerEpostFormatted = formattingUtilsService.formatEmail(body.saksbehandlerEpost)

        if (checkEmailDomain) {
            val domain = formattingUtilsService.extractEmailDomain(saksbehandlerEpostFormatted)
            if (domain != orgId) {
                throw NonMatchingEmailDomainWithOrgIdException(domain, orgId)
            }
        }

        val saksbehandler =
            if (checkSaksbehandler) {
                resourceRepository
                    .getArkivressursHrefFromPersonEmail(saksbehandlerEpostFormatted)
                    ?: throw ArchiveResourceNotFoundException(
                        saksbehandlerEpostFormatted,
                        slackAlertService,
                    )
            } else {
                ""
            }

        val hoveddokument =
            body.dokumenter.firstOrNull { it.hoveddokument }
                ?: throw IllegalStateException("No hoveddokument")

        val vedlegg = body.dokumenter.filterNot { it.hoveddokument }

        val hoveddokumentInstanceValuePerKey =
            mapHoveddokumentToInstanceValuePerKey(
                persistFile = persistFile,
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = body.sysId,
                egrunnervervJournalpostDocument = hoveddokument,
            )
        val vedleggInstanceObjects =
            mapAttachmentDocumentsToInstanceObjects(
                persistFile = persistFile,
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = body.sysId,
                egrunnervervJournalpostDocuments = vedlegg,
            )

        val valuePerKey =
            mutableMapOf(
                "saksnummer" to incomingInstance.saksnummer,
                "tittel" to body.tittel,
                "dokumentNavn" to body.dokumentNavn.orEmpty(),
                "dokumentDato" to body.dokumentDato,
                "forsendelsesmaate" to body.forsendelsesMate,
                "kommunenavn" to formattingUtilsService.formatKommunenavn(body.kommunenavn).orEmpty(),
                "knr" to body.knr,
                "gnr" to body.gnr,
                "bnr" to body.bnr,
                "fnr" to body.fnr,
                "snr" to body.snr,
                "eierforhold" to body.eierforhold.orEmpty(),
                "id" to body.id,
                "maltittel" to body.maltittel,
                "prosjektnavn" to body.prosjektnavn,
                "saksbehandlerEpost" to saksbehandlerEpostFormatted,
                "saksbehandler" to saksbehandler,
            )

        valuePerKey.putAll(hoveddokumentInstanceValuePerKey)

        return InstanceObject(
            valuePerKey,
            mutableMapOf(
                "mottakere" to body.mottakere.map(this::toInstanceObject),
                "vedlegg" to vedleggInstanceObjects,
            ),
        )
    }

    private fun toInstanceObject(egrunnervervJournalpostReceiver: EgrunnervervJournalpostReceiver): InstanceObject =
        InstanceObject(
            mapOf(
                "navn" to egrunnervervJournalpostReceiver.navn,
                "organisasjonsnummer" to
                    egrunnervervJournalpostReceiver.organisasjonsnummer
                        .takeIf(this::isOrganisasjonsnummer)
                        .orEmpty(),
                "fodselsnummer" to
                    egrunnervervJournalpostReceiver.organisasjonsnummer
                        .takeIf(this::isFodselsnummer)
                        .orEmpty(),
                "epost" to egrunnervervJournalpostReceiver.epost,
                "telefon" to egrunnervervJournalpostReceiver.telefon,
                "postadresse" to egrunnervervJournalpostReceiver.postadresse,
                "postnummer" to egrunnervervJournalpostReceiver.postnummer,
                "poststed" to egrunnervervJournalpostReceiver.poststed,
            ),
            mutableMapOf(),
        )

    private fun isFodselsnummer(number: String): Boolean = number.length == 11

    private fun isOrganisasjonsnummer(number: String): Boolean = !isFodselsnummer(number)

    private fun mapAttachmentDocumentsToInstanceObjects(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        egrunnervervJournalpostDocuments: List<EgrunnervervJournalpostDocument>,
    ): List<InstanceObject> =
        egrunnervervJournalpostDocuments.map { egrunnervervJournalpostDocument ->
            mapAttachmentDocumentToInstanceObject(
                persistFile = persistFile,
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                egrunnervervJournalpostDocument = egrunnervervJournalpostDocument,
            )
        }

    private fun getMediaType(egrunnervervJournalpostDocument: EgrunnervervJournalpostDocument): MediaType =
        MediaTypeFactory
            .getMediaType(egrunnervervJournalpostDocument.filnavn)
            .orElseThrow {
                IllegalArgumentException("No media type found for fileName=${egrunnervervJournalpostDocument.filnavn}")
            }

    private fun mapHoveddokumentToInstanceValuePerKey(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        egrunnervervJournalpostDocument: EgrunnervervJournalpostDocument,
    ): Map<String, String> {
        val mediaType = getMediaType(egrunnervervJournalpostDocument)
        val file =
            toFile(
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                egrunnervervJournalpostDocument = egrunnervervJournalpostDocument,
                type = mediaType,
            )
        val fileId = persistFile(file)
        return mapHoveddokumentAndFileIdToInstanceValuePerKey(
            egrunnervervJournalpostDocument = egrunnervervJournalpostDocument,
            mediaType = mediaType,
            fileId = fileId,
        )
    }

    private fun mapHoveddokumentAndFileIdToInstanceValuePerKey(
        egrunnervervJournalpostDocument: EgrunnervervJournalpostDocument,
        mediaType: MediaType,
        fileId: UUID,
    ): Map<String, String> =
        mapOf(
            "hoveddokumentTittel" to egrunnervervJournalpostDocument.tittel,
            "hoveddokumentFilnavn" to egrunnervervJournalpostDocument.filnavn,
            "hoveddokumentMediatype" to mediaType.toString(),
            "hoveddokumentFil" to fileId.toString(),
        )

    private fun mapAttachmentDocumentToInstanceObject(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        egrunnervervJournalpostDocument: EgrunnervervJournalpostDocument,
    ): InstanceObject {
        val mediaType = getMediaType(egrunnervervJournalpostDocument)
        val file =
            toFile(
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                egrunnervervJournalpostDocument = egrunnervervJournalpostDocument,
                type = mediaType,
            )
        val fileId = persistFile(file)
        return mapAttachmentDocumentAndFileIdToInstanceObject(
            egrunnervervJournalpostDocument = egrunnervervJournalpostDocument,
            mediaType = mediaType,
            fileId = fileId,
        )
    }

    private fun mapAttachmentDocumentAndFileIdToInstanceObject(
        egrunnervervJournalpostDocument: EgrunnervervJournalpostDocument,
        mediaType: MediaType,
        fileId: UUID,
    ): InstanceObject =
        InstanceObject(
            mapOf(
                "tittel" to egrunnervervJournalpostDocument.tittel,
                "filnavn" to egrunnervervJournalpostDocument.filnavn,
                "mediatype" to mediaType.toString(),
                "fil" to fileId.toString(),
            ),
            mutableMapOf(),
        )

    private fun toFile(
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        egrunnervervJournalpostDocument: EgrunnervervJournalpostDocument,
        type: MediaType,
    ): File =
        File(
            name = egrunnervervJournalpostDocument.filnavn,
            sourceApplicationId = sourceApplicationId,
            sourceApplicationInstanceId = sourceApplicationInstanceId,
            type = type,
            encoding = "UTF-8",
            base64Contents = egrunnervervJournalpostDocument.dokumentBase64,
        )
}
