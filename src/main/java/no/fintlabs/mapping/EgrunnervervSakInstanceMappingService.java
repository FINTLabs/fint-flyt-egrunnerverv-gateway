package no.fintlabs.mapping;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ResourceRepository;
import no.fintlabs.exceptions.ArchiveResourceNotFoundException;
import no.fintlabs.exceptions.NonMatchingEmailDomainWithOrgIdException;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.models.EgrunnervervSakInstance;
import no.fintlabs.models.EgrunnervervSakKlassering;
import no.fintlabs.models.EgrunnervervSaksPart;
import no.fintlabs.slack.SlackAlertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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

    private final FormattingUtilsService formattingUtilsService;

    private final SlackAlertService slackAlertService;

    public EgrunnervervSakInstanceMappingService(
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
            EgrunnervervSakInstance egrunnervervSakInstance,
            Function<File, Mono<UUID>> persistFile
    ) {
        return Mono.defer(() -> {

            String saksansvarligEpostFormatted = formattingUtilsService.formatEmail(egrunnervervSakInstance.getSaksansvarligEpost());

            if (checkEmailDomain) {
                String domain = formattingUtilsService.extractEmailDomain(saksansvarligEpostFormatted);
                if (!domain.equals(orgId)) {
                    throw new NonMatchingEmailDomainWithOrgIdException(domain, orgId);
                }
            }

            String saksansvarlig = "";

            if (checkSaksansvarligEpost) {
                saksansvarlig = resourceRepository
                        .getArkivressursHrefFromPersonEmail(saksansvarligEpostFormatted)
                        .orElseThrow(() -> new ArchiveResourceNotFoundException(
                                        saksansvarligEpostFormatted,
                                        slackAlertService
                                )
                        );
            }

            Map<String, String> valuePerKey = new HashMap<>();
            valuePerKey.put("sys_id", egrunnervervSakInstance.getSysId());
            valuePerKey.put("knr", egrunnervervSakInstance.getKnr());
            valuePerKey.put("gnr", egrunnervervSakInstance.getGnr());
            valuePerKey.put("bnr", egrunnervervSakInstance.getBnr());
            valuePerKey.put("fnr", egrunnervervSakInstance.getFnr());
            valuePerKey.put("snr", egrunnervervSakInstance.getSnr());
            valuePerKey.put("takstnummer", egrunnervervSakInstance.getTakstnummer());
            valuePerKey.put("tittel", egrunnervervSakInstance.getTittel());
            valuePerKey.put("saksansvarligEpost", saksansvarligEpostFormatted);
            valuePerKey.put("saksansvarlig", saksansvarlig);
            valuePerKey.put("eierforholdsnavn", egrunnervervSakInstance.getEierforholdsnavn());
            valuePerKey.put("eierforholdskode", egrunnervervSakInstance.getEierforholdskode());
            valuePerKey.put("prosjektnr", egrunnervervSakInstance.getProsjektnr());
            valuePerKey.put("prosjektnavn", egrunnervervSakInstance.getProsjektnavn());
            valuePerKey.put("kommunenavn", formattingUtilsService.formatKommunenavn(egrunnervervSakInstance.getKommunenavn()));
            valuePerKey.put("adresse", egrunnervervSakInstance.getAdresse());

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
        });
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
