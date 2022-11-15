package no.fintlabs.model.fint.caseinfo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArchiveCaseIdRequestParams {
    private final Long sourceApplicationId;
    private final String sourceApplicationInstanceId;
}
