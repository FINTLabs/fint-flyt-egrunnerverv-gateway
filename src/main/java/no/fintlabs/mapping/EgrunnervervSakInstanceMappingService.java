package no.fintlabs.mapping;

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
    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervSakInstance egrunnervervSakInstance) {
        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("sys_id", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getSysId());
        valuePerKey.put("knr", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getKnr());
        valuePerKey.put("gnr", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getGnr());
        valuePerKey.put("bnr", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getBnr());
        valuePerKey.put("fnr", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getFnr());
        valuePerKey.put("snr", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getSnr());
        valuePerKey.put("takstnummer", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getTakstnummer());
        valuePerKey.put("tittel", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getTittel());
        valuePerKey.put("saksansvarligEpost", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getSaksansvarligEpost());
        valuePerKey.put("eierforholdsnavn", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getEierforholdsnavn());
        valuePerKey.put("eierforholdskode", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getEierforholdskode());
        valuePerKey.put("prosjektnr", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getProsjektnr());
        valuePerKey.put("prosjektnavn", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getProsjektnavn());
        valuePerKey.put("kommunenavn", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getKommunenavn());
        valuePerKey.put("adresse", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getAdresse());
        valuePerKey.put("saksansvarlig", egrunnervervSakInstance.getSaksansvarlig());
        return Mono.just(
                InstanceObject.builder()
                        .valuePerKey(valuePerKey)
                        .objectCollectionPerKey(Map.of(
                                "saksparter", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getSaksparter()
                                        .stream()
                                        .map(this::toInstanceObject)
                                        .toList(),
                                "klasseringer", egrunnervervSakInstance.getEgrunnervervSakInstanceDto().getKlasseringer()
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
