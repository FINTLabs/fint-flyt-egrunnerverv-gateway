package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.model.EgrunnervervArchiveInstance;
import no.fintlabs.model.EgrunnervervArchiveInstanceToMap;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/egrunnerverv/instances/{orgNr}")
public class EgrunnervervInstanceController {


    private final InstanceProcessor<EgrunnervervArchiveInstanceToMap> archiveInstanceProcessor;
    private final ResourceRepository resourceRepository;

    public EgrunnervervInstanceController(InstanceProcessor<EgrunnervervArchiveInstanceToMap> archiveInstanceProcessor, ResourceRepository resourceRepository) {
        this.archiveInstanceProcessor = archiveInstanceProcessor;
        this.resourceRepository = resourceRepository;
    }


    @PostMapping("archive")
    public Mono<ResponseEntity<?>> postArchiveInstance(
            @RequestBody EgrunnervervArchiveInstance egrunnervervArchiveInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return authenticationMono.flatMap(
                authentication -> archiveInstanceProcessor.processInstance(
                        authentication,
                        getEgrunnervervArchiveInstanceToMap(egrunnervervArchiveInstance)
                )
        );
    }

    private EgrunnervervArchiveInstanceToMap getEgrunnervervArchiveInstanceToMap(EgrunnervervArchiveInstance egrunnervervArchiveInstance) {
        Optional<String> saksansvarlig = resourceRepository.getUsername(egrunnervervArchiveInstance.getSaksansvarligEpost());
        if (saksansvarlig.isEmpty()) {
            throw new ArchiveResourceNotFoundException(egrunnervervArchiveInstance.getSaksansvarligEpost());
        }

        ModelMapper modelMapper = new ModelMapper();

        EgrunnervervArchiveInstanceToMap egrunnervervArchiveInstanceToMap = modelMapper.map(egrunnervervArchiveInstance, EgrunnervervArchiveInstanceToMap.class);
        egrunnervervArchiveInstanceToMap.setSaksansvarlig(saksansvarlig.get());
        return egrunnervervArchiveInstanceToMap;
    }

//    @PostMapping("document")
//    public Mono<ResponseEntity<?>> postDocumentInstance(
//            @RequestBody EgrunnervervDocumentInstance egrunnervervDocumentInstance,
//            @AuthenticationPrincipal Mono<Authentication> authenticationMono
//    ) {
//        return authenticationMono.flatMap(authentication -> documentInstanceProcessor.processInstance(authentication, egrunnervervDocumentInstance));
//    }

}
