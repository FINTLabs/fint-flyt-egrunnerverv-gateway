package no.fintlabs.models;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;

@Getter
@Jacksonized
@EqualsAndHashCode(callSuper = false)
@Builder
public class EgrunnervervSakInstance {
    @NotNull
    private final EgrunnervervSakInstanceDto egrunnervervSakInstanceDto;
    @NotNull
    private final String saksansvarlig;
}