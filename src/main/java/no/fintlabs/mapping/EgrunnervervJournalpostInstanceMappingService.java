package no.fintlabs.mapping;

import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.models.EgrunnervervJournalpostDocument;
import no.fintlabs.models.EgrunnervervJournalpostInstance;
import no.fintlabs.models.EgrunnervervJournalpostReceiver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.*;

@Service
public class EgrunnervervJournalpostInstanceMappingService implements InstanceMapper<EgrunnervervJournalpostInstance> {

    private final FileClient fileClient;

    public EgrunnervervJournalpostInstanceMappingService(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervJournalpostInstance egrunnervervJournalpostInstance) {
        EgrunnervervJournalpostDocument hoveddokument = egrunnervervJournalpostInstance
                .getEgrunnervervJournalpostInstanceDto().getDokumenter()
                .stream()
                .filter(EgrunnervervJournalpostDocument::getHoveddokument)
                .findFirst()
                .orElseThrow(() -> {
                    throw new IllegalStateException("No hoveddokument");
                });

        List<EgrunnervervJournalpostDocument> vedlegg = egrunnervervJournalpostInstance
                .getEgrunnervervJournalpostInstanceDto().getDokumenter()
                .stream()
                .filter(dokument -> !dokument.getHoveddokument())
                .toList();

        Mono<Map<String, String>> hoveddokumentInstanceValuePerKeyMono = mapHoveddokumentToInstanceValuePerKey(
                sourceApplicationId,
                egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getSysId(),
                hoveddokument
        );
        Mono<List<InstanceObject>> vedleggInstanceObjectsMono = mapAttachmentDocumentsToInstanceObjects(
                sourceApplicationId,
                egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getSysId(),
                vedlegg
        );

        return Mono.zip(
                        hoveddokumentInstanceValuePerKeyMono,
                        vedleggInstanceObjectsMono
                )
                .map((Tuple2<Map<String, String>, List<InstanceObject>> hovedDokumentValuePerKeyAndVedleggInstanceObjects) -> {
                            HashMap<String, String> valuePerKey = new HashMap<>(
                                    Map.of(
                                            "saksnummer", Optional.ofNullable(egrunnervervJournalpostInstance.getSaksnummer()).orElse(""),
                                            "tittel", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getTittel()).orElse(""),
                                            "dokumentNavn", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getDokumentNavn()).orElse(""),
                                            "dokumentDato", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getDokumentDato()).orElse(""),
                                            "forsendelsesmaate", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getForsendelsesMate()).orElse("")
                                    )
                            );
                            valuePerKey.putAll(hovedDokumentValuePerKeyAndVedleggInstanceObjects.getT1());
                            return InstanceObject.builder()
                                    .valuePerKey(valuePerKey)
                                    .objectCollectionPerKey(
                                            Map.of(
                                                    "mottakere", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getMottakere()
                                                            .stream()
                                                            .map(this::toInstanceObject)
                                                            .toList(),
                                                    "vedlegg", hovedDokumentValuePerKeyAndVedleggInstanceObjects.getT2()
                                            ))
                                    .build();
                        }

                );
    }

    private InstanceObject toInstanceObject(EgrunnervervJournalpostReceiver egrunnervervJournalpostReceiver) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "navn", Optional.ofNullable(egrunnervervJournalpostReceiver.getNavn()).orElse(""),
                        "organisasjonsnummer", Optional.ofNullable(egrunnervervJournalpostReceiver.getOrganisasjonsnummer()).orElse(""),
                        "epost", Optional.ofNullable(egrunnervervJournalpostReceiver.getEpost()).orElse(""),
                        "telefon", Optional.ofNullable(egrunnervervJournalpostReceiver.getTelefon()).orElse(""),
                        "postadresse", Optional.ofNullable(egrunnervervJournalpostReceiver.getPostadresse()).orElse(""),
                        "postnummer", Optional.ofNullable(egrunnervervJournalpostReceiver.getPostnummer()).orElse(""),
                        "poststed", Optional.ofNullable(egrunnervervJournalpostReceiver.getPoststed()).orElse("")
                ))
                .build();
    }

    private Mono<List<InstanceObject>> mapAttachmentDocumentsToInstanceObjects(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            List<EgrunnervervJournalpostDocument> egrunnervervJournalpostDocuments
    ) {
        return Flux.fromIterable(egrunnervervJournalpostDocuments)
                .flatMap(egrunnervervJournalpostDocument -> mapAttachmentDocumentToInstanceObject(
                        sourceApplicationId, sourceApplicationInstanceId, egrunnervervJournalpostDocument
                ))
                .collectList();
    }

    private Mono<Map<String, String>> mapHoveddokumentToInstanceValuePerKey(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument
    ) {
        File file = toFile(
                sourceApplicationId,
                sourceApplicationInstanceId,
                egrunnervervJournalpostDocument,
                MediaType.APPLICATION_PDF
        );
        return fileClient.postFile(file)
                .map(fileId -> mapHoveddokumentAndFileIdToInstanceValuePerKey(egrunnervervJournalpostDocument, fileId));
    }

    private Map<String, String> mapHoveddokumentAndFileIdToInstanceValuePerKey(
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument, UUID fileId
    ) {
        return Map.of(
                "hoveddokumentTittel", Optional.ofNullable(egrunnervervJournalpostDocument.getTittel()).orElse(""),
                "hoveddokumentFilnavn", Optional.ofNullable(egrunnervervJournalpostDocument.getFilnavn()).orElse(""),
                "hoveddokumentFil", fileId.toString()
        );
    }

    private Mono<InstanceObject> mapAttachmentDocumentToInstanceObject(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument
    ) {
        File file = toFile(
                sourceApplicationId,
                sourceApplicationInstanceId,
                egrunnervervJournalpostDocument,
                MediaType.APPLICATION_PDF // TODO eivindmorch 29/03/2023 : Parse from fileName ending
        );
        return fileClient.postFile(file)
                .map(fileId -> mapAttachmentDocumentAndFileIdToInstanceObject(egrunnervervJournalpostDocument, fileId));
    }

    private InstanceObject mapAttachmentDocumentAndFileIdToInstanceObject(
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument, UUID fileId
    ) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "tittel", Optional.ofNullable(egrunnervervJournalpostDocument.getTittel()).orElse(""),
                        "filnavn", Optional.ofNullable(egrunnervervJournalpostDocument.getFilnavn()).orElse(""),
                        "fil", fileId.toString()
                ))
                .build();
    }

    private File toFile(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument,
            MediaType type
    ) {
        return File
                .builder()
                .name(egrunnervervJournalpostDocument.getFilnavn())
                .type(type)
                .sourceApplicationId(sourceApplicationId)
                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                .encoding("UTF-8")
                .base64Contents(egrunnervervJournalpostDocument.getDokumentBase64())
                .build();
    }

}
