package no.fintlabs.model.egrunnerverv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.validation.UniqueElementIds;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EgrunnervervInstance {

    @NotNull
    @Valid
    private EgrunnervervInstanceMetadata metadata;

    @NotEmpty
    @UniqueElementIds
    @Valid
    private List<@NotNull EgrunnervervInstanceElement> elements;

    @Valid
    private List<@NotNull EgrunnervervDocument> documents = new ArrayList<>();

}
