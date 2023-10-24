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
public class EgrunnervervJournalpostInstance {
    @NotNull
    private final EgrunnervervJournalpostInstanceBody egrunnervervJournalpostInstanceBody;
    @NotNull
    private final String saksnummer;
}
