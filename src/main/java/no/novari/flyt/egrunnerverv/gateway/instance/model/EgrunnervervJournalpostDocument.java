package no.novari.flyt.egrunnerverv.gateway.instance.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class EgrunnervervJournalpostDocument {
    @NotNull
    private final String tittel;
    @NotNull
    private final Boolean hoveddokument;
    @NotNull
    private final String filnavn;
    @NotEmpty
    private final String dokumentBase64;
}
