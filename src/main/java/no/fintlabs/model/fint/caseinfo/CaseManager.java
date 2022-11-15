package no.fintlabs.model.fint.caseinfo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CaseManager {
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String email;
    private final String phone;
}
