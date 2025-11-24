package no.novari.flyt.egrunnerverv.gateway.instance.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
