package no.fintlabs.mapping;

import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.instance.InstanceElement;
import no.fintlabs.model.EgrunnervervArchiveCasePart;
import no.fintlabs.model.EgrunnervervArchiveClassification;
import no.fintlabs.model.EgrunnervervArchiveInstance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class EgrunnervervArchiveInstanceMappingService implements InstanceMapper<EgrunnervervArchiveInstance> {
    @Override
    public Mono<InstanceElement> map(Long sourceApplicationId, EgrunnervervArchiveInstance egrunnervervArchiveInstance) {
        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("sys_id", egrunnervervArchiveInstance.getSys_id());
        valuePerKey.put("knr", egrunnervervArchiveInstance.getKnr());
        valuePerKey.put("gnr", egrunnervervArchiveInstance.getGnr());
        valuePerKey.put("bnr", egrunnervervArchiveInstance.getBnr());
        valuePerKey.put("fnr", egrunnervervArchiveInstance.getFnr());
        valuePerKey.put("snr", egrunnervervArchiveInstance.getSnr());
        valuePerKey.put("takstnummer", egrunnervervArchiveInstance.getTakstnummer());
        valuePerKey.put("tittel", egrunnervervArchiveInstance.getTittel());
        valuePerKey.put("eierforholdsnavn", egrunnervervArchiveInstance.getEierforholdsnavn());
        valuePerKey.put("eierforholdskode", egrunnervervArchiveInstance.getEierforholdskode());
        valuePerKey.put("prosjektnr", egrunnervervArchiveInstance.getProsjektnr());
        valuePerKey.put("prosjektnavn", egrunnervervArchiveInstance.getProsjektnavn());
        valuePerKey.put("kommunenavn", egrunnervervArchiveInstance.getKommunenavn());
        valuePerKey.put("adresse", egrunnervervArchiveInstance.getAdresse());
        return Mono.just(
                InstanceElement.builder()
                        .valuePerKey(valuePerKey)
                        .elementCollectionPerKey(Map.of(
                                "saksparter", egrunnervervArchiveInstance.getSaksparter()
                                        .stream()
                                        .map(this::toInstanceElement)
                                        .toList(),
                                "klasseringer", egrunnervervArchiveInstance.getKlasseringer()
                                        .stream()
                                        .map(this::toInstanceElement)
                                        .toList()
                        ))
                        .build()
        );
    }


    private InstanceElement toInstanceElement(EgrunnervervArchiveCasePart egrunnervervArchiveCasePart) {
        return InstanceElement
                .builder()
                .valuePerKey(Map.of(
                        "sakspartRolleId", egrunnervervArchiveCasePart.getSakspartRolleId(),
                        "navn", egrunnervervArchiveCasePart.getNavn(),
                        "organisasjonsnummer", egrunnervervArchiveCasePart.getOrganisasjonsnummer(),
                        "epost", egrunnervervArchiveCasePart.getEpost(),
                        "telefon", egrunnervervArchiveCasePart.getTelefon(),
                        "postadresse", egrunnervervArchiveCasePart.getPostadresse(),
                        "poststed", egrunnervervArchiveCasePart.getPoststed()
                ))
                .build();
    }

    private InstanceElement toInstanceElement(EgrunnervervArchiveClassification egrunnervervArchiveClassification) {
        return InstanceElement
                .builder()
                .valuePerKey(Map.of(
                        "ordningsprinsipp", egrunnervervArchiveClassification.getOrdningsprinsipp(),
                        "ordningsverdi", egrunnervervArchiveClassification.getOrdningsverdi(),
                        "beskrivelse", egrunnervervArchiveClassification.getBeskrivelse(),
                        "sortering", egrunnervervArchiveClassification.getSortering(),
                        "untattOffentlighet", egrunnervervArchiveClassification.getUntattOffentlighet()
                ))
                .build();
    }
}
