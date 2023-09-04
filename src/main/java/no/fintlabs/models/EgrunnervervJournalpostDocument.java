package no.fintlabs.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
    @NotNull
    private final String dokumentBase64;

    @Override
    public String toString() {
        return "Sensitive data omitted";
    }
}
