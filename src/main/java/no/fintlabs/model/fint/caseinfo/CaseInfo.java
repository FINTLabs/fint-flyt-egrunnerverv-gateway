package no.fintlabs.model.fint.caseinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CaseInfo {
    @JsonProperty("instanceId")
    private final String sourceApplicationInstanceId;
    private final String archiveCaseId;
    private final CaseManager caseManager;
    private final AdministrativeUnit administrativeUnit;
    private final CaseStatus status;
}
