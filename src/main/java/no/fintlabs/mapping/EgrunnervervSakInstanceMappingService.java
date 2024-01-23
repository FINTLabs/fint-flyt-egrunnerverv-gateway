package no.fintlabs.mapping;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ResourceRepository;
import no.fintlabs.exceptions.ArchiveResourceNotFoundException;
import no.fintlabs.exceptions.NonMatchingEmailDomainWithOrgIdException;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.models.EgrunnervervSakInstance;
import no.fintlabs.models.EgrunnervervSakInstancePrepared;
import no.fintlabs.models.EgrunnervervSakKlassering;
import no.fintlabs.models.EgrunnervervSaksPart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static no.fintlabs.mapping.EmailUtils.extractEmailDomain;

@Service
@Slf4j
public class EgrunnervervSakInstanceMappingService implements InstanceMapper<EgrunnervervSakInstance> {

    @Value("${fint.flyt.egrunnerverv.checkSaksansvarligEpost:true}")
    boolean checkSaksansvarligEpost;

    @Value("${fint.flyt.egrunnerverv.checkEmailDomain:true}")
    boolean checkEmailDomain;
    private final ResourceRepository resourceRepository;

    @Value("${fint.org-id}")
    private String orgId;

    public EgrunnervervSakInstanceMappingService(
            ResourceRepository resourceRepository
    ) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, EgrunnervervSakInstance egrunnervervSakInstance) {
        return Mono.defer(() -> {
            EgrunnervervSakInstancePrepared egrunnervervSakInstancePrepared = EgrunnervervSakInstancePrepared
                    .builder()
                    .sysId(egrunnervervSakInstance.getSysId())
                    .knr(egrunnervervSakInstance.getKnr())
                    .gnr(egrunnervervSakInstance.getGnr())
                    .bnr(egrunnervervSakInstance.getBnr())
                    .fnr(egrunnervervSakInstance.getFnr())
                    .snr(egrunnervervSakInstance.getSnr())
                    .takstnummer(egrunnervervSakInstance.getTakstnummer())
                    .tittel(egrunnervervSakInstance.getTittel())
                    .saksansvarligEpost(egrunnervervSakInstance.getSaksansvarligEpost().trim())
                    .eierforholdsnavn(egrunnervervSakInstance.getEierforholdsnavn())
                    .eierforholdskode(egrunnervervSakInstance.getEierforholdskode())
                    .prosjektnr(egrunnervervSakInstance.getProsjektnr())
                    .prosjektnavn(egrunnervervSakInstance.getProsjektnavn())
                    .kommunenavn(egrunnervervSakInstance.getKommunenavn())
                    .adresse(egrunnervervSakInstance.getAdresse())
                    .saksparter(egrunnervervSakInstance.getSaksparter())
                    .klasseringer(egrunnervervSakInstance.getKlasseringer())
                    .build();

            if (checkEmailDomain) {
                String domain = extractEmailDomain(egrunnervervSakInstancePrepared.getSaksansvarligEpost());
                if (!domain.equals(orgId)) {
                    throw new NonMatchingEmailDomainWithOrgIdException(domain, orgId);
                }
            }

            String saksansvarlig = "";

            if (checkSaksansvarligEpost) {
                saksansvarlig = resourceRepository
                        .getArkivressursHrefFromPersonEmail(egrunnervervSakInstancePrepared.getSaksansvarligEpost())
                        .orElseThrow(() -> new ArchiveResourceNotFoundException(
                                        egrunnervervSakInstancePrepared.getSaksansvarligEpost()
                                )
                        );
            }

            egrunnervervSakInstancePrepared.setSaksansvarlig(saksansvarlig);

            Map<String, String> valuePerKey = getStringStringMap(egrunnervervSakInstancePrepared);
            return Mono.just(
                    InstanceObject.builder()
                            .valuePerKey(valuePerKey)
                            .objectCollectionPerKey(Map.of(
                                    "saksparter", egrunnervervSakInstancePrepared.getSaksparter()
                                            .stream()
                                            .map(this::toInstanceObject)
                                            .toList(),
                                    "klasseringer", egrunnervervSakInstancePrepared.getKlasseringer()
                                            .stream()
                                            .map(this::toInstanceObject)
                                            .toList()
                            ))
                            .build()
            );
        });
    }

    private static Map<String, String> getStringStringMap(EgrunnervervSakInstancePrepared egrunnervervSakInstancePrepared) {
        Map<String, String> valuePerKey = new HashMap<>();
        valuePerKey.put("sys_id", egrunnervervSakInstancePrepared.getSysId());
        valuePerKey.put("knr", egrunnervervSakInstancePrepared.getKnr());
        valuePerKey.put("gnr", egrunnervervSakInstancePrepared.getGnr());
        valuePerKey.put("bnr", egrunnervervSakInstancePrepared.getBnr());
        valuePerKey.put("fnr", egrunnervervSakInstancePrepared.getFnr());
        valuePerKey.put("snr", egrunnervervSakInstancePrepared.getSnr());
        valuePerKey.put("takstnummer", egrunnervervSakInstancePrepared.getTakstnummer());
        valuePerKey.put("tittel", egrunnervervSakInstancePrepared.getTittel());
        valuePerKey.put("saksansvarligEpost", egrunnervervSakInstancePrepared.getSaksansvarligEpost());
        valuePerKey.put("saksansvarlig", egrunnervervSakInstancePrepared.getSaksansvarlig());
        valuePerKey.put("eierforholdsnavn", egrunnervervSakInstancePrepared.getEierforholdsnavn());
        valuePerKey.put("eierforholdskode", egrunnervervSakInstancePrepared.getEierforholdskode());
        valuePerKey.put("prosjektnr", egrunnervervSakInstancePrepared.getProsjektnr());
        valuePerKey.put("prosjektnavn", egrunnervervSakInstancePrepared.getProsjektnavn());
        valuePerKey.put("kommunenavn", egrunnervervSakInstancePrepared.getKommunenavn());
        valuePerKey.put("adresse", egrunnervervSakInstancePrepared.getAdresse());
        return valuePerKey;
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
