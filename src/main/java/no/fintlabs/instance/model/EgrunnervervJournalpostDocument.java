package no.fintlabs.instance.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
