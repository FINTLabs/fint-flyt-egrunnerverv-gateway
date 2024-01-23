package no.fintlabs.mapping;

import no.fintlabs.ResourceRepository;
import no.fintlabs.exceptions.ArchiveResourceNotFoundException;
import no.fintlabs.exceptions.NonMatchingEmailDomainWithOrgIdException;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.*;

import static no.fintlabs.mapping.EmailUtils.extractEmailDomain;


@Service
public class EgrunnervervJournalpostInstanceMappingService implements InstanceMapper<EgrunnervervJournalpostInstance> {

    @Value("${fint.flyt.egrunnerverv.checkSaksbehandler:true}")
    boolean checkSaksbehandler;

    @Value("${fint.flyt.egrunnerverv.checkEmailDomain:true}")
    boolean checkEmailDomain;
    private final FileClient fileClient;

    private final ResourceRepository resourceRepository;

    @Value("${fint.org-id}")
    private String orgId;

    public EgrunnervervJournalpostInstanceMappingService(
            FileClient fileClient,
            ResourceRepository resourceRepository) {
        this.fileClient = fileClient;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervJournalpostInstance egrunnervervJournalpostInstance) {
        return Mono.defer(() -> {
            EgrunnervervJournalpostInstancePrepared egrunnervervJournalpostInstancePrepared =
                    EgrunnervervJournalpostInstancePrepared
                            .builder()
                            .saksnummer(egrunnervervJournalpostInstance.getSaksnummer())
                            .sysId(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSysId())
                            .tittel(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getTittel())
                            .dokumentNavn(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getDokumentNavn())
                            .dokumentDato(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getDokumentDato())
                            .forsendelsesMate(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getForsendelsesMate())
                            .kommunenavn(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getKommunenavn())
                            .knr(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getKnr())
                            .gnr(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getGnr())
                            .bnr(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getBnr())
                            .fnr(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getFnr())
                            .snr(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSnr())
                            .eierforhold(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getEierforhold())
                            .id(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getId())
                            .maltittel(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getMaltittel())
                            .prosjektnavn(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getProsjektnavn())
                            .saksbehandlerEpost(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSaksbehandlerEpost().trim())
                            .mottakere(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getMottakere())
                            .dokumenter(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getDokumenter())
                            .build();

            if (checkEmailDomain) {
                String domain = extractEmailDomain(egrunnervervJournalpostInstancePrepared.getSaksbehandlerEpost());
                if (!domain.equals(orgId)) {
                    throw new NonMatchingEmailDomainWithOrgIdException(domain, orgId);
                }
            }

            String saksbehandler;
            if (checkSaksbehandler) {
                saksbehandler = resourceRepository.getArkivressursHrefFromPersonEmail(egrunnervervJournalpostInstancePrepared.getSaksbehandlerEpost())
                        .orElseThrow(() -> new ArchiveResourceNotFoundException(egrunnervervJournalpostInstancePrepared.getSaksbehandlerEpost()));
            } else {
                saksbehandler = "";
            }

            egrunnervervJournalpostInstancePrepared.setSaksbehandler(saksbehandler);

            EgrunnervervJournalpostDocument hoveddokument = egrunnervervJournalpostInstancePrepared
                    .getDokumenter()
                    .stream()
                    .filter(EgrunnervervJournalpostDocument::getHoveddokument)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No hoveddokument"));

            List<EgrunnervervJournalpostDocument> vedlegg = egrunnervervJournalpostInstancePrepared
                    .getDokumenter()
                    .stream()
                    .filter(dokument -> !dokument.getHoveddokument())
                    .toList();

            Mono<Map<String, String>> hoveddokumentInstanceValuePerKeyMono = mapHoveddokumentToInstanceValuePerKey(
                    sourceApplicationId,
                    egrunnervervJournalpostInstancePrepared.getSysId(),
                    hoveddokument
            );
            Mono<List<InstanceObject>> vedleggInstanceObjectsMono = mapAttachmentDocumentsToInstanceObjects(
                    sourceApplicationId,
                    egrunnervervJournalpostInstancePrepared.getSysId(),
                    vedlegg
            );

            return Mono.zip(
                            hoveddokumentInstanceValuePerKeyMono,
                            vedleggInstanceObjectsMono
                    )
                    .map((Tuple2<Map<String, String>, List<InstanceObject>> hovedDokumentValuePerKeyAndVedleggInstanceObjects) -> {

                                HashMap<String, String> valuePerKey = new HashMap<>();
                                valuePerKey.put("saksnummer", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getSaksnummer()).orElse(""));
                                valuePerKey.put("tittel", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getTittel()).orElse(""));
                                valuePerKey.put("dokumentNavn", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getDokumentNavn()).orElse(""));
                                valuePerKey.put("dokumentDato", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getDokumentDato()).orElse(""));
                                valuePerKey.put("forsendelsesmaate", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getForsendelsesMate()).orElse(""));
                                valuePerKey.put("kommunenavn", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getKommunenavn()).orElse(""));
                                valuePerKey.put("knr", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getKnr()).orElse(""));
                                valuePerKey.put("gnr", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getGnr()).orElse(""));
                                valuePerKey.put("bnr", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getBnr()).orElse(""));
                                valuePerKey.put("fnr", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getFnr()).orElse(""));
                                valuePerKey.put("snr", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getSnr()).orElse(""));
                                valuePerKey.put("eierforhold", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getEierforhold()).orElse(""));
                                valuePerKey.put("id", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getId()).orElse(""));
                                valuePerKey.put("maltittel", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getMaltittel()).orElse(""));
                                valuePerKey.put("prosjektnavn", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getProsjektnavn()).orElse(""));
                                valuePerKey.put("saksbehandlerEpost", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getSaksbehandlerEpost()).orElse(""));
                                valuePerKey.put("saksbehandler", Optional.ofNullable(egrunnervervJournalpostInstancePrepared.getSaksbehandler()).orElse(""));

                                valuePerKey.putAll(hovedDokumentValuePerKeyAndVedleggInstanceObjects.getT1());
                                return InstanceObject.builder()
                                        .valuePerKey(valuePerKey)
                                        .objectCollectionPerKey(
                                                Map.of(
                                                        "mottakere", egrunnervervJournalpostInstancePrepared.getMottakere()
                                                                .stream()
                                                                .map(this::toInstanceObject)
                                                                .toList(),
                                                        "vedlegg", hovedDokumentValuePerKeyAndVedleggInstanceObjects.getT2()
                                                ))
                                        .build();
                            }

                    );
        });
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
