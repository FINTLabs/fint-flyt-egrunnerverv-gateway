package no.fintlabs.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class EgrunnervervArchiveInstanceToMap extends EgrunnervervArchiveInstance {
    @NotNull
    private String saksansvarlig;
}