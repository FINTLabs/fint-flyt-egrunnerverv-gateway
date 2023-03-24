package no.fintlabs.mapping;

import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.model.EgrunnervervArchiveCasePart;
import no.fintlabs.model.EgrunnervervArchiveClassification;
import no.fintlabs.model.EgrunnervervArchiveInstanceToMap;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class EgrunnervervArchiveInstanceMappingService implements InstanceMapper<EgrunnervervArchiveInstanceToMap> {

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervArchiveInstanceToMap egrunnervervArchiveInstanceToMap) {
        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("sys_id", egrunnervervArchiveInstanceToMap.getSysId());
        valuePerKey.put("knr", egrunnervervArchiveInstanceToMap.getKnr());
        valuePerKey.put("gnr", egrunnervervArchiveInstanceToMap.getGnr());
        valuePerKey.put("bnr", egrunnervervArchiveInstanceToMap.getBnr());
        valuePerKey.put("fnr", egrunnervervArchiveInstanceToMap.getFnr());
        valuePerKey.put("snr", egrunnervervArchiveInstanceToMap.getSnr());
        valuePerKey.put("takstnummer", egrunnervervArchiveInstanceToMap.getTakstnummer());
        valuePerKey.put("tittel", egrunnervervArchiveInstanceToMap.getTittel());
        valuePerKey.put("eierforholdsnavn", egrunnervervArchiveInstanceToMap.getEierforholdsnavn());
        valuePerKey.put("eierforholdskode", egrunnervervArchiveInstanceToMap.getEierforholdskode());
        valuePerKey.put("prosjektnr", egrunnervervArchiveInstanceToMap.getProsjektnr());
        valuePerKey.put("prosjektnavn", egrunnervervArchiveInstanceToMap.getProsjektnavn());
        valuePerKey.put("kommunenavn", egrunnervervArchiveInstanceToMap.getKommunenavn());
        valuePerKey.put("adresse", egrunnervervArchiveInstanceToMap.getAdresse());
        valuePerKey.put("saksansvarlig", egrunnervervArchiveInstanceToMap.getSaksansvarlig());
        return Mono.just(
                InstanceObject.builder()
                        .valuePerKey(valuePerKey)
                        .objectCollectionPerKey(Map.of(
                                "saksparter", egrunnervervArchiveInstanceToMap.getSaksparter()
                                        .stream()
                                        .map(this::toInstanceObject)
                                        .toList(),
                                "klasseringer", egrunnervervArchiveInstanceToMap.getKlasseringer()
                                        .stream()
                                        .map(this::toInstanceObject)
                                        .toList()
                        ))
                        .build()
        );
    }


    private InstanceObject toInstanceObject(EgrunnervervArchiveCasePart egrunnervervArchiveCasePart) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "sakspartRolleId", egrunnervervArchiveCasePart.getSakspartRolleId(),
                        "navn", egrunnervervArchiveCasePart.getNavn(),
                        "organisasjonsnummer", egrunnervervArchiveCasePart.getOrganisasjonsnummer(),
                        "epost", egrunnervervArchiveCasePart.getEpost(),
                        "telefon", egrunnervervArchiveCasePart.getTelefon(),
                        "postadresse", egrunnervervArchiveCasePart.getPostadresse(),
                        "postnummer", egrunnervervArchiveCasePart.getPostnummer(),
                        "poststed", egrunnervervArchiveCasePart.getPoststed()
                ))
                .build();
    }

    private InstanceObject toInstanceObject(EgrunnervervArchiveClassification egrunnervervArchiveClassification) {
        return InstanceObject
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
