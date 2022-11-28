package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EgrunnervervArchiveCasePart {
    private String sakspartRolleId;
    private String navn;
    private String organisasjonsnummer;
    private String epost;
    private String telefon;
    private String postadresse;
    private String postnummer;
    private String poststed;
}
