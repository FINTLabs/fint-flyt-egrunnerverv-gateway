package no.fintlabs.instance.mapping;

import no.fintlabs.exception.ArchiveResourceNotFoundException;
import no.fintlabs.exception.NonMatchingEmailDomainWithOrgIdException;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.instance.ResourceRepository;
import no.fintlabs.instance.model.EgrunnervervJournalpostDocument;
import no.fintlabs.instance.model.EgrunnervervJournalpostInstance;
import no.fintlabs.instance.model.EgrunnervervJournalpostReceiver;
import no.fintlabs.slack.SlackAlertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.*;
import java.util.function.Function;


@Service
public class EgrunnervervJournalpostInstanceMappingService implements InstanceMapper<EgrunnervervJournalpostInstance> {

    @Value("${fint.flyt.egrunnerverv.checkSaksbehandler:true}")
    boolean checkSaksbehandler;

    @Value("${fint.flyt.egrunnerverv.checkEmailDomain:true}")
    boolean checkEmailDomain;
    private final ResourceRepository resourceRepository;
    private final FormattingUtilsService formattingUtilsService;

    @Value("${fint.org-id}")
    private String orgId;

    private final SlackAlertService slackAlertService;

    public EgrunnervervJournalpostInstanceMappingService(
            ResourceRepository resourceRepository,
            FormattingUtilsService formattingUtilsService,
            SlackAlertService slackAlertService
    ) {
        this.resourceRepository = resourceRepository;
        this.formattingUtilsService = formattingUtilsService;
        this.slackAlertService = slackAlertService;
    }

    @Override
    public Mono<InstanceObject> map(
            Long sourceApplicationId,
            EgrunnervervJournalpostInstance egrunnervervJournalpostInstance,
            Function<File, Mono<UUID>> persistFile
    ) {
        return Mono.defer(() -> {

            String saksbehandlerEpostFormatted = formattingUtilsService.formatEmail(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSaksbehandlerEpost());

            if (checkEmailDomain) {
                String domain = formattingUtilsService.extractEmailDomain(saksbehandlerEpostFormatted);
                if (!domain.equals(orgId)) {
                    throw new NonMatchingEmailDomainWithOrgIdException(domain, orgId);
                }
            }

            String saksbehandler;
            if (checkSaksbehandler) {
                saksbehandler = resourceRepository.getArkivressursHrefFromPersonEmail(saksbehandlerEpostFormatted)
                        .orElseThrow(() -> new ArchiveResourceNotFoundException(
                                saksbehandlerEpostFormatted,
                                slackAlertService
                        ));
            } else {
                saksbehandler = "";
            }

            EgrunnervervJournalpostDocument hoveddokument = egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody()
                    .getDokumenter()
                    .stream()
                    .filter(EgrunnervervJournalpostDocument::getHoveddokument)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No hoveddokument"));

            List<EgrunnervervJournalpostDocument> vedlegg = egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody()
                    .getDokumenter()
                    .stream()
                    .filter(dokument -> !dokument.getHoveddokument())
                    .toList();

            Mono<Map<String, String>> hoveddokumentInstanceValuePerKeyMono = mapHoveddokumentToInstanceValuePerKey(
                    persistFile,
                    sourceApplicationId,
                    egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSysId(),
                    hoveddokument
            );
            Mono<List<InstanceObject>> vedleggInstanceObjectsMono = mapAttachmentDocumentsToInstanceObjects(
                    persistFile,
                    sourceApplicationId,
                    egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSysId(),
                    vedlegg
            );

            HashMap<String, String> valuePerKey = new HashMap<>();
            valuePerKey.put("saksnummer", Optional.ofNullable(egrunnervervJournalpostInstance.getSaksnummer()).orElse(""));
            valuePerKey.put("tittel", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getTittel()).orElse(""));
            valuePerKey.put("dokumentNavn", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getDokumentNavn()).orElse(""));
            valuePerKey.put("dokumentDato", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getDokumentDato()).orElse(""));
            valuePerKey.put("forsendelsesmaate", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getForsendelsesMate()).orElse(""));
            valuePerKey.put("kommunenavn", Optional.ofNullable(formattingUtilsService.formatKommunenavn(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getKommunenavn())).orElse(""));
            valuePerKey.put("knr", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getKnr()).orElse(""));
            valuePerKey.put("gnr", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getGnr()).orElse(""));
            valuePerKey.put("bnr", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getBnr()).orElse(""));
            valuePerKey.put("fnr", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getFnr()).orElse(""));
            valuePerKey.put("snr", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getSnr()).orElse(""));
            valuePerKey.put("eierforhold", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getEierforhold()).orElse(""));
            valuePerKey.put("id", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getId()).orElse(""));
            valuePerKey.put("maltittel", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getMaltittel()).orElse(""));
            valuePerKey.put("prosjektnavn", Optional.ofNullable(egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getProsjektnavn()).orElse(""));
            valuePerKey.put("saksbehandlerEpost", Optional.ofNullable(saksbehandlerEpostFormatted).orElse(""));
            valuePerKey.put("saksbehandler", Optional.ofNullable(saksbehandler).orElse(""));

            return Mono.zip(
                            hoveddokumentInstanceValuePerKeyMono,
                            vedleggInstanceObjectsMono
                    )
                    .map((Tuple2<Map<String, String>, List<InstanceObject>> hovedDokumentValuePerKeyAndVedleggInstanceObjects) -> {

                                valuePerKey.putAll(hovedDokumentValuePerKeyAndVedleggInstanceObjects.getT1());
                                return InstanceObject.builder()
                                        .valuePerKey(valuePerKey)
                                        .objectCollectionPerKey(
                                                Map.of(
                                                        "mottakere", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceBody().getMottakere()
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
            Function<File, Mono<UUID>> persistFile,
            Long sourceApplicationId,
            String sourceApplicationInstanceId,
            List<EgrunnervervJournalpostDocument> egrunnervervJournalpostDocuments
    ) {
        return Flux.fromIterable(egrunnervervJournalpostDocuments)
                .flatMap(egrunnervervJournalpostDocument -> mapAttachmentDocumentToInstanceObject(
                        persistFile,
                        sourceApplicationId,
                        sourceApplicationInstanceId,
                        egrunnervervJournalpostDocument
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
            Function<File, Mono<UUID>> persistFile,
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
        return persistFile.apply(file)
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
            Function<File, Mono<UUID>> persistFile,
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
        return persistFile.apply(file)
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
