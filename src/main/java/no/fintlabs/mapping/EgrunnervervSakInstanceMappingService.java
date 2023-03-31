package no.fintlabs.mapping;

import no.fintlabs.ResourceRepository;
import no.fintlabs.exceptions.ArchiveResourceNotFoundException;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.models.EgrunnervervSakInstance;
import no.fintlabs.models.EgrunnervervSakKlassering;
import no.fintlabs.models.EgrunnervervSaksPart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class EgrunnervervSakInstanceMappingService implements InstanceMapper<EgrunnervervSakInstance> {

    private final ResourceRepository resourceRepository;

    public EgrunnervervSakInstanceMappingService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervSakInstance egrunnervervSakInstance) {

        String saksansvarlig = resourceRepository.getSaksansvarligHref(egrunnervervSakInstance.getSaksansvarligEpost())
                .orElseThrow(() -> new ArchiveResourceNotFoundException(egrunnervervSakInstance.getSaksansvarligEpost()));

        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("sys_id", egrunnervervSakInstance.getSysId());
        valuePerKey.put("knr", egrunnervervSakInstance.getKnr());
        valuePerKey.put("gnr", egrunnervervSakInstance.getGnr());
        valuePerKey.put("bnr", egrunnervervSakInstance.getBnr());
        valuePerKey.put("fnr", egrunnervervSakInstance.getFnr());
        valuePerKey.put("snr", egrunnervervSakInstance.getSnr());
        valuePerKey.put("takstnummer", egrunnervervSakInstance.getTakstnummer());
        valuePerKey.put("tittel", egrunnervervSakInstance.getTittel());
        valuePerKey.put("saksansvarligEpost", egrunnervervSakInstance.getSaksansvarligEpost());
        valuePerKey.put("eierforholdsnavn", egrunnervervSakInstance.getEierforholdsnavn());
        valuePerKey.put("eierforholdskode", egrunnervervSakInstance.getEierforholdskode());
        valuePerKey.put("prosjektnr", egrunnervervSakInstance.getProsjektnr());
        valuePerKey.put("prosjektnavn", egrunnervervSakInstance.getProsjektnavn());
        valuePerKey.put("kommunenavn", egrunnervervSakInstance.getKommunenavn());
        valuePerKey.put("adresse", egrunnervervSakInstance.getAdresse());
        valuePerKey.put("saksansvarlig", saksansvarlig);
        return Mono.just(
                InstanceObject.builder()
                        .valuePerKey(valuePerKey)
                        .objectCollectionPerKey(Map.of(
                                "saksparter", egrunnervervSakInstance.getSaksparter()
                                        .stream()
                                        .map(this::toInstanceObject)
                                        .toList(),
                                "klasseringer", egrunnervervSakInstance.getKlasseringer()
                                        .stream()
                                        .map(this::toInstanceObject)
                                        .toList()
                        ))
                        .build()
        );
    }

    private InstanceObject toInstanceObject(EgrunnervervSaksPart egrunnervervSaksPart) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "navn", egrunnervervSaksPart.getNavn(),
                        "organisasjonsnummer", egrunnervervSaksPart.getOrganisasjonsnummer(),
                        "epost", egrunnervervSaksPart.getEpost(),
                        "telefon", egrunnervervSaksPart.getTelefon(),
                        "postadresse", egrunnervervSaksPart.getPostadresse(),
                        "postnummer", egrunnervervSaksPart.getPostnummer(),
                        "poststed", egrunnervervSaksPart.getPoststed()
                ))
                .build();
    }

    private InstanceObject toInstanceObject(EgrunnervervSakKlassering egrunnervervSakKlassering) {
        return InstanceObject
                .builder()
                .valuePerKey(Map.of(
                        "ordningsprinsipp", egrunnervervSakKlassering.getOrdningsprinsipp(),
                        "ordningsverdi", egrunnervervSakKlassering.getOrdningsverdi(),
                        "beskrivelse", egrunnervervSakKlassering.getBeskrivelse(),
                        "sortering", egrunnervervSakKlassering.getSortering(),
                        "untattOffentlighet", egrunnervervSakKlassering.getUntattOffentlighet()
                ))
                .build();
    }
}
