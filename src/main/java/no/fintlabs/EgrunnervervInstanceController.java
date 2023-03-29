package no.fintlabs;

import no.fintlabs.exceptions.ArchiveCaseNotFoundException;
import no.fintlabs.exceptions.ArchiveResourceNotFoundException;
import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseRequestService;
import no.fintlabs.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/egrunnerverv/instances/{orgNr}")
public class EgrunnervervInstanceController {


    private final InstanceProcessor<EgrunnervervSakInstance> sakInstanceProcessor;
    private final InstanceProcessor<EgrunnervervJournalpostInstance> journalpostInstanceProcessor;
    private final ArchiveCaseRequestService archiveCaseRequestService;
    private final ResourceRepository resourceRepository;
    private final EgrunnervervSimpleInstanceProducerService egrunnervervSimpleInstanceProducerService;


    public EgrunnervervInstanceController(
            InstanceProcessor<EgrunnervervSakInstance> sakInstanceProcessor,
            InstanceProcessor<EgrunnervervJournalpostInstance> journalpostInstanceProcessor,
            ArchiveCaseRequestService archiveCaseRequestService,
            ResourceRepository resourceRepository,
            EgrunnervervSimpleInstanceProducerService egrunnervervSimpleInstanceProducerService
    ) {
        this.sakInstanceProcessor = sakInstanceProcessor;
        this.journalpostInstanceProcessor = journalpostInstanceProcessor;
        this.archiveCaseRequestService = archiveCaseRequestService;
        this.resourceRepository = resourceRepository;
        this.egrunnervervSimpleInstanceProducerService = egrunnervervSimpleInstanceProducerService;
    }


    @PostMapping("archive")
    public Mono<ResponseEntity<?>> postSakInstance(
            @RequestBody EgrunnervervSakInstanceDto egrunnervervSakInstanceDto,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {

        String saksansvarlig = resourceRepository.getUsername(egrunnervervSakInstanceDto.getSaksansvarligEpost())
                .orElseThrow(() -> new ArchiveResourceNotFoundException(egrunnervervSakInstanceDto.getSaksansvarligEpost()));

        return authenticationMono.flatMap(
                authentication -> sakInstanceProcessor.processInstance(
                                authentication,
                                EgrunnervervSakInstance.builder()
                                        .egrunnervervSakInstanceDto(egrunnervervSakInstanceDto)
                                        .saksansvarlig(saksansvarlig)
                                        .build()
                        )
                        .doOnNext(responseEntity -> {
                            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                                egrunnervervSimpleInstanceProducerService.publishSimpleSakInstance(
                                        EgrunnervervSimpleInstance.builder()
                                                .sysId(egrunnervervSakInstanceDto.getSysId())
                                                .tableName(egrunnervervSakInstanceDto.getTable())
                                                .build()
                                );
                            }
                        })
        );
    }

    @PostMapping("document")
    public Mono<ResponseEntity<?>> postJournalpostInstance(
            @RequestBody EgrunnervervJournalpostInstanceDto egrunnervervJournalpostInstanceDto,
            @RequestParam("id") String saksnummer,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {

        archiveCaseRequestService.getByArchiveCaseId(saksnummer)
                .orElseThrow(() -> new ArchiveCaseNotFoundException(saksnummer));

        return authenticationMono.flatMap(
                authentication -> journalpostInstanceProcessor.processInstance(
                        authentication,
                        EgrunnervervJournalpostInstance.builder()
                                .egrunnervervJournalpostInstanceDto(egrunnervervJournalpostInstanceDto)
                                .saksnummer(saksnummer)
                                .build()
                ).doOnNext(responseEntity -> {
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        egrunnervervSimpleInstanceProducerService.publishSimpleJournalpostInstance(
                                EgrunnervervSimpleInstance.builder()
                                        .sysId(egrunnervervJournalpostInstanceDto.getSysId())
                                        .tableName(egrunnervervJournalpostInstanceDto.getTable())
                                        .build()
                        );
                    }
                })
        );
    }

}
