package no.fintlabs.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EgrunnervervSimpleInstance {
    @NotNull
    private String sysId;
    @NotNull
    private String tableName;
}