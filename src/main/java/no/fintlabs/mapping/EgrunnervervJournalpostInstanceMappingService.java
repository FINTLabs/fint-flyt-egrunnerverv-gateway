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
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EgrunnervervJournalpostInstanceMappingService implements InstanceMapper<EgrunnervervJournalpostInstance> {

    private final FileClient fileClient;

    public EgrunnervervJournalpostInstanceMappingService(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervJournalpostInstance egrunnervervJournalpostInstance) {
        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("saksnummer", egrunnervervJournalpostInstance.getSaksnummer());
        valuePerKey.put("sys_id", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getSysId());
        valuePerKey.put("tittel", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getTittel());
        valuePerKey.put("dokumentNavn", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getDokumentNavn());
        valuePerKey.put("dokumentDato", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getDokumentDato());
        valuePerKey.put("forsendelsesmaate", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getForsendelsesMate());

        return Mono.just(
                InstanceObject.builder()
                        .valuePerKey(valuePerKey)
                        .objectCollectionPerKey(Map.of(
                                "mottakere", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getMottakere()
                                        .stream()
                                        .map(this::toInstanceObject)
                                        .toList(),
                                "dokumenter", egrunnervervJournalpostInstance.getEgrunnervervJournalpostInstanceDto().getDokumenter()
                                        .stream()
                                        .map(this::toInstanceObject)
                                        .toList()
                        ))
                        .build()
        );
    }

    private InstanceObject toInstanceObject(EgrunnervervJournalpostReceiver egrunnervervJournalpostReceiver) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "navn", egrunnervervJournalpostReceiver.getNavn(),
                        "organisasjonsnummer", egrunnervervJournalpostReceiver.getOrganisasjonsnummer(),
                        "epost", egrunnervervJournalpostReceiver.getEpost(),
                        "telefon", egrunnervervJournalpostReceiver.getTelefon(),
                        "postadresse", egrunnervervJournalpostReceiver.getPostadresse(),
                        "postnummer", egrunnervervJournalpostReceiver.getPostnummer(),
                        "poststed", egrunnervervJournalpostReceiver.getPoststed()
                ))
                .build();
    }

    private InstanceObject toInstanceObject(EgrunnervervJournalpostDocument egrunnervervJournalpostDocument) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "tittel", egrunnervervJournalpostDocument.getTittel(),
                        "hoveddokument", egrunnervervJournalpostDocument.getHoveddokument(),
                        "filnavn", egrunnervervJournalpostDocument.getFilnavn(),
                        "dokumentBase64", egrunnervervJournalpostDocument.getDokumentBase64()
                ))
                .build();
    }

    private Mono<UUID> mapPdfFileToFileId(Long sourceApplicationId, String sourceApplicationInstanceId, EgrunnervervJournalpostDocument egrunnervervJournalpostDocument) {
        return fileClient.postFile(
                File
                        .builder()
                        .name(egrunnervervJournalpostDocument.getFilnavn())
                        .type(MediaType.APPLICATION_PDF)
                        .sourceApplicationId(sourceApplicationId)
                        .sourceApplicationInstanceId(sourceApplicationInstanceId)
                        .encoding("UTF-8")
                        .base64Contents(egrunnervervJournalpostDocument.getDokumentBase64())
                        .build()
        );
    }
}
