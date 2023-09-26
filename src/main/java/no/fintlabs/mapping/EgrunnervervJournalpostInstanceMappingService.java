package no.fintlabs.mapping;

import no.fintlabs.ResourceRepository;
import no.fintlabs.exceptions.ArchiveCaseNotFoundException;
import no.fintlabs.exceptions.ArchiveResourceNotFoundException;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseRequestService;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.models.EgrunnervervJournalpostDocument;
import no.fintlabs.models.EgrunnervervJournalpostInstance;
import no.fintlabs.models.EgrunnervervJournalpostInstanceBody;
import no.fintlabs.models.EgrunnervervJournalpostReceiver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.*;

@Service
public class EgrunnervervJournalpostInstanceMappingService implements InstanceMapper<EgrunnervervJournalpostInstance> {

    @Value("${fint.flyt.egrunnerverv.checkSaksbehandler:true}")
    boolean checkSaksbehandler;
    private final ArchiveCaseRequestService archiveCaseRequestService;
    private final FileClient fileClient;

    private final ResourceRepository resourceRepository;


    public EgrunnervervJournalpostInstanceMappingService(
            ArchiveCaseRequestService archiveCaseRequestService,
            FileClient fileClient,
            ResourceRepository resourceRepository) {
        this.archiveCaseRequestService = archiveCaseRequestService;
        this.fileClient = fileClient;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervJournalpostInstance egrunnervervJournalpostInstance) {

        archiveCaseRequestService.getByArchiveCaseId(egrunnervervJournalpostInstance.getSaksnummer())
                .orElseThrow(() -> new ArchiveCaseNotFoundException(egrunnervervJournalpostInstance.getSaksnummer()));

        EgrunnervervJournalpostInstanceBody egrunnervervJournalpostInstanceBody =
                egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody();

        EgrunnervervJournalpostDocument hoveddokument = egrunnervervJournalpostInstanceBody
                .getDokumenter()
                .stream()
                .filter(EgrunnervervJournalpostDocument::getHoveddokument)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hoveddokument"));

        List<EgrunnervervJournalpostDocument> vedlegg = egrunnervervJournalpostInstanceBody
                .getDokumenter()
                .stream()
                .filter(dokument -> !dokument.getHoveddokument())
                .toList();

        Mono<Map<String, String>> hoveddokumentInstanceValuePerKeyMono = mapHoveddokumentToInstanceValuePerKey(
                sourceApplicationId,
                egrunnervervJournalpostInstanceBody.getSysId(),
                hoveddokument
        );
        Mono<List<InstanceObject>> vedleggInstanceObjectsMono = mapAttachmentDocumentsToInstanceObjects(
                sourceApplicationId,
                egrunnervervJournalpostInstanceBody.getSysId(),
                vedlegg
        );

        return Mono.zip(
                        hoveddokumentInstanceValuePerKeyMono,
                        vedleggInstanceObjectsMono
                )
                .map((Tuple2<Map<String, String>, List<InstanceObject>> hovedDokumentValuePerKeyAndVedleggInstanceObjects) -> {

                            String saksbehandler = "";
                            if (checkSaksbehandler) {
                                saksbehandler = resourceRepository.getArkivressursHrefFromPersonEmail(egrunnervervJournalpostInstanceBody.getSaksbehandlerEpost())
                                        .orElseThrow(() -> new ArchiveResourceNotFoundException(egrunnervervJournalpostInstanceBody.getSaksbehandlerEpost()));
                            }

                            HashMap<String, String> valuePerKey = new HashMap<>();
                            valuePerKey.put("saksnummer", Optional.ofNullable(egrunnervervJournalpostInstance.getSaksnummer()).orElse(""));
                            valuePerKey.put("tittel", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getTittel()).orElse(""));
                            valuePerKey.put("dokumentNavn", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getDokumentNavn()).orElse(""));
                            valuePerKey.put("dokumentDato", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getDokumentDato()).orElse(""));
                            valuePerKey.put("forsendelsesmaate", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getForsendelsesMate()).orElse(""));
                            valuePerKey.put("kommunenavn", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getKommunenavn()).orElse(""));
                            valuePerKey.put("knr", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getKnr()).orElse(""));
                            valuePerKey.put("gnr", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getGnr()).orElse(""));
                            valuePerKey.put("bnr", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getBnr()).orElse(""));
                            valuePerKey.put("fnr", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getFnr()).orElse(""));
                            valuePerKey.put("snr", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getSnr()).orElse(""));
                            valuePerKey.put("eierforhold", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getEierforhold()).orElse(""));
                            valuePerKey.put("id", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getId()).orElse(""));
                            valuePerKey.put("maltittel", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getMaltittel()).orElse(""));
                            valuePerKey.put("prosjektnavn", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getProsjektnavn()).orElse(""));
                            valuePerKey.put("saksbehandlerEpost", Optional.ofNullable(egrunnervervJournalpostInstanceBody.getSaksbehandlerEpost()).orElse(""));
                            valuePerKey.put("saksbehandler", Optional.ofNullable(saksbehandler).orElse(""));

                            valuePerKey.putAll(hovedDokumentValuePerKeyAndVedleggInstanceObjects.getT1());
                            return InstanceObject.builder()
                                    .valuePerKey(valuePerKey)
                                    .objectCollectionPerKey(
                                            Map.of(
                                                    "mottakere", egrunnervervJournalpostInstanceBody.getMottakere()
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
                        "organisasjonsnummer", Optional.ofNullable(egrunnervervJournalpostReceiver.getOrganisasjonsnummer())
                                .filter(this::isOrganisasjonsnummer)
                                .orElse(""),
                        "fodselsnummer", Optional.ofNullable(egrunnervervJournalpostReceiver.getOrganisasjonsnummer())
                                .filter(this::isFodselsnummer)
                                .orElse(""),
                        "epost", Optional.ofNullable(egrunnervervJournalpostReceiver.getEpost()).orElse(""),
                        "telefon", Optional.ofNullable(egrunnervervJournalpostReceiver.getTelefon()).orElse(""),
                        "postadresse", Optional.ofNullable(egrunnervervJournalpostReceiver.getPostadresse()).orElse(""),
                        "postnummer", Optional.ofNullable(egrunnervervJournalpostReceiver.getPostnummer()).orElse(""),
                        "poststed", Optional.ofNullable(egrunnervervJournalpostReceiver.getPoststed()).orElse("")
                ))
                .build();
    }

    private boolean isFodselsnummer(String number) {
        return number.length() == 11;
    }

    private boolean isOrganisasjonsnummer(String number) {
        return !isFodselsnummer(number);
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

    private MediaType getMediaType(EgrunnervervJournalpostDocument egrunnervervJournalpostDocument) {
        return MediaTypeFactory.getMediaType(egrunnervervJournalpostDocument.getFilnavn())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No media type found for fileName=" + egrunnervervJournalpostDocument.getFilnavn()
                ));
    }

    private Mono<Map<String, String>> mapHoveddokumentToInstanceValuePerKey(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument
    ) {
        MediaType mediaType = getMediaType(egrunnervervJournalpostDocument);
        File file = toFile(
                sourceApplicationId,
                sourceApplicationInstanceId,
                egrunnervervJournalpostDocument,
                mediaType
        );
        return fileClient.postFile(file)
                .map(fileId -> mapHoveddokumentAndFileIdToInstanceValuePerKey(egrunnervervJournalpostDocument, mediaType, fileId));
    }

    private Map<String, String> mapHoveddokumentAndFileIdToInstanceValuePerKey(
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument,
            MediaType mediaType,
            UUID fileId
    ) {
        return Map.of(
                "hoveddokumentTittel", Optional.ofNullable(egrunnervervJournalpostDocument.getTittel()).orElse(""),
                "hoveddokumentFilnavn", Optional.ofNullable(egrunnervervJournalpostDocument.getFilnavn()).orElse(""),
                "hoveddokumentMediatype", mediaType.toString(),
                "hoveddokumentFil", fileId.toString()
        );
    }

    private Mono<InstanceObject> mapAttachmentDocumentToInstanceObject(
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument
    ) {
        MediaType mediaType = getMediaType(egrunnervervJournalpostDocument);
        File file = toFile(
                sourceApplicationId,
                sourceApplicationInstanceId,
                egrunnervervJournalpostDocument,
                mediaType
        );
        return fileClient.postFile(file)
                .map(fileId -> mapAttachmentDocumentAndFileIdToInstanceObject(
                        egrunnervervJournalpostDocument,
                        mediaType,
                        fileId));
    }

    private InstanceObject mapAttachmentDocumentAndFileIdToInstanceObject(
            EgrunnervervJournalpostDocument egrunnervervJournalpostDocument,
            MediaType mediaType,
            UUID fileId
    ) {

        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "tittel", Optional.ofNullable(egrunnervervJournalpostDocument.getTittel()).orElse(""),
                        "filnavn", Optional.ofNullable(egrunnervervJournalpostDocument.getFilnavn()).orElse(""),
                        "mediatype", mediaType.toString(),
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
