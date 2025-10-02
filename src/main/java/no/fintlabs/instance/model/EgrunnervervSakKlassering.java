package no.fintlabs.instance.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class EgrunnervervSakKlassering {
    @NotNull
    private final String ordningsprinsipp;
    @NotNull
    private final String ordningsverdi;
    @NotNull
    private final String beskrivelse;
    @NotNull
    private final String sortering;
    @NotNull
    private final String untattOffentlighet;
}
