package no.fintlabs.models;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;


@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class EgrunnervervSimpleInstance {
    @NotNull
    private String sysId;
    @NotNull
    private String tableName;
}