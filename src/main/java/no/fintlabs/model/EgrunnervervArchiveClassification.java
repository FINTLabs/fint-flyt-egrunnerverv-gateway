package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EgrunnervervArchiveClassification {
    @NotNull
    private String ordningsprinsipp;
    @NotNull
    private String ordningsverdi;
    @NotNull
    private String beskrivelse;
    @NotNull
    private String sortering;
    @NotNull
    private String untattOffentlighet;
}
