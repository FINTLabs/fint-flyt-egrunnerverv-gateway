package no.fintlabs;

import no.fintlabs.caseinfo.CaseInfoMappingService;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.*;
import no.fintlabs.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import no.fintlabs.model.egrunnerverv.EgrunnervervInstance;
import no.fintlabs.model.fint.Integration;
import no.fintlabs.model.fint.caseinfo.AdministrativeUnit;
import no.fintlabs.model.fint.caseinfo.CaseInfo;
import no.fintlabs.model.fint.caseinfo.CaseManager;
import no.fintlabs.model.fint.caseinfo.CaseStatus;
import no.fintlabs.model.fint.instance.Instance;
import no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil;
import no.fintlabs.validation.EgrunnervervInstanceValidationService;
import no.fintlabs.validation.InstanceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;
import static no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil.getSourceApplicationId;


/*
 /egrunnerverv/archive?orgid=11111111111 (arkiv-sak)
  /egrunnerverv/document?orgid=11111111111 (arkiv-journalpost)
  /egrunnerverv/status?id=<arkivnummer>&orgid=11111111111
 */


@RestController
@RequestMapping(EXTERNAL_API + "/egrunnerverv/instanser")
public class EgrunnervervInstanceController {

    private final EgrunnervervInstanceValidationService egrunnervervInstanceValidationService;
    private final EgrunnervervInstanceMapper egrunnervervInstanceMapper;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;
    private final IntegrationRequestProducerService integrationRequestProducerService;
    private final ArchiveCaseIdRequestService archiveCaseIdRequestService;
    private final ArchiveCaseRequestService archiveCaseRequestService;
    private final CaseInfoMappingService caseInfoMappingService;

    public EgrunnervervInstanceController(
            EgrunnervervInstanceValidationService egrunnervervInstanceValidationService,
            EgrunnervervInstanceMapper egrunnervervInstanceMapper,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService,
            IntegrationRequestProducerService integrationRequestProducerService,
            ArchiveCaseIdRequestService archiveCaseIdRequestService,
            ArchiveCaseRequestService archiveCaseRequestService,
            CaseInfoMappingService caseInfoMappingService) {
        this.egrunnervervInstanceValidationService = egrunnervervInstanceValidationService;
        this.egrunnervervInstanceMapper = egrunnervervInstanceMapper;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
        this.integrationRequestProducerService = integrationRequestProducerService;
        this.archiveCaseIdRequestService = archiveCaseIdRequestService;
        this.archiveCaseRequestService = archiveCaseRequestService;
        this.caseInfoMappingService = caseInfoMappingService;
        WebClient webClient = WebClient.builder()
                .baseUrl("$")
                .build();
    }

    @GetMapping("{sourceApplicationInstanceId}/saksinformasjon")
    public Mono<ResponseEntity<CaseInfo>> getInstanceCaseInfo(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @PathVariable String sourceApplicationInstanceId,
            @RequestParam Optional<Boolean> returnMockData
    ) {
        return authenticationMono.map(authentication -> getCaseInfo(
                authentication,
                sourceApplicationInstanceId,
                returnMockData.orElse(false)
        ));
    }

    public ResponseEntity<CaseInfo> getCaseInfo(
            Authentication authentication,
            String sourceApplicationInstanceId,
            boolean returnMockData
    ) {
        if (returnMockData) {
            return ResponseEntity.ok(createMockCaseInfo(sourceApplicationInstanceId));
        }
        return archiveCaseIdRequestService.getArchiveCaseId(
                        getSourceApplicationId(authentication),
                        sourceApplicationInstanceId
                )
                .flatMap(archiveCaseRequestService::getByArchiveCaseId)
                .map(caseResource -> caseInfoMappingService.toCaseInfo(sourceApplicationInstanceId, caseResource))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Case with sourceApplicationInstanceId=%s could not be found", sourceApplicationInstanceId)
                ));
    }

    private CaseInfo createMockCaseInfo(String sourceApplicationInstanceId) {
        return CaseInfo
                .builder()
                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                .archiveCaseId("2021/02")
                .caseManager(
                        CaseManager
                                .builder()
                                .firstName("Ola")
                                .middleName(null)
                                .lastName("Nordmann")
                                .email("ola.normann@domain.com")
                                .phone("12345678")
                                .build()
                )
                .administrativeUnit(
                        AdministrativeUnit
                                .builder()
                                .name("VGGLEM Skolemiljø og Kommunikasjon")
                                .build()
                )
                .status(
                        CaseStatus
                                .builder()
                                .name("Under behandling")
                                .code("B")
                                .build()
                )
                .build();
    }

    @PostMapping
    public Mono<ResponseEntity<?>> postInstance(
            @RequestBody EgrunnervervInstance egrunnervervInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono) {

        return authenticationMono.map(authentication -> processInstance(egrunnervervInstance, authentication));
    }

    private ResponseEntity<?> processInstance(EgrunnervervInstance egrunnervervInstance, Authentication authentication) {

        InstanceFlowHeaders.InstanceFlowHeadersBuilder instanceFlowHeadersBuilder = InstanceFlowHeaders.builder();

        try {
            Long sourceApplicationId = ClientAuthorizationUtil.getSourceApplicationId(authentication);

            instanceFlowHeadersBuilder.correlationId(UUID.randomUUID());
            instanceFlowHeadersBuilder.sourceApplicationId(sourceApplicationId);
            if (egrunnervervInstance.getMetadata() != null) {

                String sourceApplicationIntegrationId = egrunnervervInstance.getMetadata().getFormId();

                instanceFlowHeadersBuilder.sourceApplicationIntegrationId(sourceApplicationIntegrationId);
                instanceFlowHeadersBuilder.sourceApplicationInstanceId(egrunnervervInstance.getMetadata().getInstanceId());


                if (!egrunnervervInstance.getMetadata().getFormId().isBlank()) {

                    SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId =
                            SourceApplicationIdAndSourceApplicationIntegrationId
                                    .builder()
                                    .sourceApplicationId(sourceApplicationId)
                                    .sourceApplicationIntegrationId(sourceApplicationIntegrationId)
                                    .build();

                    Integration integration = integrationRequestProducerService
                            .get(sourceApplicationIdAndSourceApplicationIntegrationId)
                            .orElseThrow(() -> new NoIntegrationException(sourceApplicationIdAndSourceApplicationIntegrationId));

                    if (integration.getState() == Integration.State.DEACTIVATED) {
                        throw new IntegrationDeactivatedException(integration);
                    }

                    instanceFlowHeadersBuilder.integrationId(integration.getId());

                }
            }

            egrunnervervInstanceValidationService.validate(egrunnervervInstance).ifPresent((validationErrors) -> {
                throw new InstanceValidationException(validationErrors);
            });

            Instance instance = egrunnervervInstanceMapper.toInstance(egrunnervervInstance);

            receivedInstanceEventProducerService.publish(
                    instanceFlowHeadersBuilder.build(),
                    instance
            );

            return ResponseEntity.accepted().build();

        } catch (InstanceValidationException e) {
            instanceReceivalErrorEventProducerService.publishInstanceValidationErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Validation error" + (e.getValidationErrors().size() > 1 ? "s:" : ": ") +
                            e.getValidationErrors()
                                    .stream()
                                    .map(error -> "'" + error.getFieldPath() + " " + error.getErrorMessage() + "'")
                                    .toList()
            );
        } catch (NoIntegrationException e) {
            instanceReceivalErrorEventProducerService.publishNoIntegrationFoundErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    e.getMessage()
            );
        } catch (IntegrationDeactivatedException e) {
            instanceReceivalErrorEventProducerService.publishIntegrationDeactivatedErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    e.getMessage()
            );
        } catch (RuntimeException e) {
            instanceReceivalErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeadersBuilder.build());
            throw e;
        }

    }

}
