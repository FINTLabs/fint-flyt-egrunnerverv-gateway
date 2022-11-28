package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EgrunnervervArchiveClassification {
    private String ordningsprinsipp;
    private String ordningsverdi;
    private String beskrivelse;
    private String sortering;
    private String untattOffentlighet;
}
