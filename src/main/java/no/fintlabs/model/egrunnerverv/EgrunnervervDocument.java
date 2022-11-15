package no.fintlabs.model.egrunnerverv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EgrunnervervDocument {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotBlank
    private String encoding;

    @NotBlank
    private String base64;

}
