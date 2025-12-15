package no.novari.flyt.egrunnerverv.gateway.instance;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstance;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstanceBody;
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakInstance;
import no.novari.flyt.instance.gateway.InstanceProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static no.novari.flyt.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/egrunnerverv/instances/{orgNr}")
@Slf4j
public class EgrunnervervInstanceController {


    private final InstanceProcessor<EgrunnervervSakInstance> sakInstanceProcessor;
    private final InstanceProcessor<EgrunnervervJournalpostInstance> journalpostInstanceProcessor;

    public EgrunnervervInstanceController(
            InstanceProcessor<EgrunnervervSakInstance> sakInstanceProcessor,
            InstanceProcessor<EgrunnervervJournalpostInstance> journalpostInstanceProcessor
    ) {
        this.sakInstanceProcessor = sakInstanceProcessor;
        this.journalpostInstanceProcessor = journalpostInstanceProcessor;
    }


    @PostMapping("archive")
    public Mono<ResponseEntity<?>> postSakInstance(
            @RequestBody EgrunnervervSakInstance egrunnervervSakInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono.flatMap(
                authentication -> sakInstanceProcessor.processInstance(
                        authentication,
                        egrunnervervSakInstance
                )
        );
    }

    @PostMapping("document")
    public Mono<ResponseEntity<?>> postJournalpostInstance(
            @RequestBody EgrunnervervJournalpostInstanceBody egrunnervervJournalpostInstanceBody,
            @RequestParam("id") String saksnummer,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        EgrunnervervJournalpostInstance egrunnervervJournalpostInstance = EgrunnervervJournalpostInstance.builder()
                .egrunnervervJournalpostInstanceBody(egrunnervervJournalpostInstanceBody)
                .saksnummer(saksnummer)
                .build();

        return authenticationMono.flatMap(
                authentication -> journalpostInstanceProcessor.processInstance(
                        authentication,
                        egrunnervervJournalpostInstance
                )
        );
    }

}
