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
public class EgrunnervervArchiveCasePart {
    @NotNull
    private String sakspartRolleId;
    @NotNull
    private String navn;
    @NotNull
    private String organisasjonsnummer;
    @NotNull
    private String epost;
    @NotNull
    private String telefon;
    @NotNull
    private String postadresse;
    @NotNull
    private String postnummer;
    @NotNull
    private String poststed;
}
