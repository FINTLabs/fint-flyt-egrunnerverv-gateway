package no.novari.flyt.egrunnerverv.gateway.dispatch.converting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.novari.flyt.egrunnerverv.gateway.dispatch.kafka.CaseRequestService;
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity;
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity;
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.JournalpostReceipt;
import no.novari.flyt.egrunnerverv.gateway.dispatch.model.SakReceipt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Service
public class InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService {

    private final String tablenameSak;
    private final String tablenameJournalpost;
    private final CaseRequestService caseRequestService;
    private final ObjectMapper objectMapper;
    private final JournalpostToInstanceReceiptDispatchEntityConvertingService journalpostToInstanceReceiptDispatchEntityConvertingService;

    public static final String EGRUNNERVERV_DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final String SAK = "sak";
    private static final String JOURNALPOST = "journalpost";

    public InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService(
            @Value("${fint.flyt.egrunnerverv.dispatch.tablenameSak}") String tablenameSak,
            @Value("${fint.flyt.egrunnerverv.dispatch.tablenameJournalpost}") String tablenameJournalpost,
            CaseRequestService caseRequestService,
            ObjectMapper objectMapper,
            JournalpostToInstanceReceiptDispatchEntityConvertingService journalpostToInstanceReceiptDispatchEntityConvertingService
    ) {
        this.tablenameSak = tablenameSak;
        this.tablenameJournalpost = tablenameJournalpost;
        this.caseRequestService = caseRequestService;
        this.objectMapper = objectMapper;
        this.journalpostToInstanceReceiptDispatchEntityConvertingService =
                journalpostToInstanceReceiptDispatchEntityConvertingService;
    }

    public Optional<InstanceReceiptDispatchEntity> convert(InstanceHeadersEntity instanceHeadersEntity) {
        return switch (instanceHeadersEntity.getSourceApplicationIntegrationId()) {
            case SAK -> convertSak(instanceHeadersEntity);
            case JOURNALPOST -> convertJournalpost(instanceHeadersEntity);
            default -> throw new IllegalStateException(
                    "Unexpected value: " + instanceHeadersEntity.getSourceApplicationIntegrationId()
            );
        };
    }

    private Optional<InstanceReceiptDispatchEntity> convertSak(InstanceHeadersEntity instanceHeadersEntity) {
        String archiveInstanceId = instanceHeadersEntity.getArchiveInstanceId();
        String sourceApplicationInstanceId = instanceHeadersEntity.getSourceApplicationInstanceId();

        return caseRequestService.getByMappeId(archiveInstanceId)
                .map(sakResource -> {
                    SakReceipt sakReceipt = SakReceipt.builder()
                            .arkivnummer(archiveInstanceId)
                            .opprettelse_i_elements_fullfort(formatOrNull(sakResource.getOpprettetDato()))
                            .build();

                    String uri = buildUri(tablenameSak, sourceApplicationInstanceId, "arkivnummer");

                    return InstanceReceiptDispatchEntity.builder()
                            .sourceApplicationInstanceId(sourceApplicationInstanceId)
                            .instanceReceipt(toJson(sakReceipt))
                            .classType(SakReceipt.class)
                            .uri(uri)
                            .build();
                });
    }

    private Optional<InstanceReceiptDispatchEntity> convertJournalpost(InstanceHeadersEntity instanceHeadersEntity) {
        String sourceApplicationInstanceId = instanceHeadersEntity.getSourceApplicationInstanceId();

        String[] splitArchiveInstanceId = instanceHeadersEntity.getArchiveInstanceId().split("-");
        String caseId = splitArchiveInstanceId[0];
        Long journalpostNummer = Long.parseLong(
                splitArchiveInstanceId[1]
                        .replace("[", "")
                        .replace("]", "")
        );
        return caseRequestService.getByMappeId(caseId)
                .map(sakResource -> {
                    JournalpostReceipt journalpostReceipt =
                            journalpostToInstanceReceiptDispatchEntityConvertingService
                                    .map(sakResource, journalpostNummer);

                    String uri = buildUri(tablenameJournalpost, sourceApplicationInstanceId, "journalpostnr");

                    InstanceReceiptDispatchEntity instanceReceiptDispatchEntity;

                    instanceReceiptDispatchEntity = InstanceReceiptDispatchEntity.builder()
                            .sourceApplicationInstanceId(sourceApplicationInstanceId)
                            .instanceReceipt(toJson(journalpostReceipt))
                            .classType(JournalpostReceipt.class)
                            .uri(uri)
                            .build();

                    return instanceReceiptDispatchEntity;
                });
    }

    private String buildUri(String tableName, String instanceId, String fieldName) {
        return UriComponentsBuilder.newInstance()
                .pathSegment(tableName, instanceId)
                .queryParam("sysparm_fields", fieldName)
                .queryParam("sysparm_query_no_domain", "true")
                .toUriString();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize instance receipt: " + value, e);
        }
    }

    private String formatOrNull(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern(EGRUNNERVERV_DATETIME_FORMAT));
    }
}
