package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.model.EgrunnervervArchiveInstance;
import no.fintlabs.model.EgrunnervervDocumentInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;


/*
  /egrunnerverv/archive?orgid=11111111111 (arkiv-sak)
  /egrunnerverv/document?id=<arkivnummer>&orgid=11111111111 (arkiv-journalpost)
  /egrunnerverv/status?id=<arkivnummer>&orgid=11111111111
 */


@RestController
@RequestMapping(EXTERNAL_API + "/egrunnerverv/instances/{orgNr}")
public class EgrunnervervInstanceController {


    private final InstanceProcessor<EgrunnervervArchiveInstance> archiveInstanceProcessor;
    private final InstanceProcessor<EgrunnervervDocumentInstance> documentInstanceProcessor;

    public EgrunnervervInstanceController(InstanceProcessor<EgrunnervervArchiveInstance> archiveInstanceProcessor, InstanceProcessor<EgrunnervervDocumentInstance> documentInstanceProcessor) {
        this.archiveInstanceProcessor = archiveInstanceProcessor;
        this.documentInstanceProcessor = documentInstanceProcessor;
    }


    @PostMapping("archive")
    public Mono<ResponseEntity<?>> postArchiveInstance(
            @RequestBody EgrunnervervArchiveInstance egrunnervervArchiveInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono.flatMap(authentication -> archiveInstanceProcessor.processInstance(authentication, egrunnervervArchiveInstance));
    }


    @PostMapping("document")
    public Mono<ResponseEntity<?>> postDocumentInstance(
            @RequestBody EgrunnervervDocumentInstance egrunnervervDocumentInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono.flatMap(authentication -> documentInstanceProcessor.processInstance(authentication, egrunnervervDocumentInstance));

    }

}
