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
public class EgrunnervervJournalpostReceiver {
    @NotNull
    private final String navn;
    @NotNull
    private final String organisasjonsnummer;
    @NotNull
    private final String epost;
    @NotNull
    private final String telefon;
    @NotNull
    private final String postadresse;
    @NotNull
    private final String postnummer;
    @NotNull
    private final String poststed;
}
