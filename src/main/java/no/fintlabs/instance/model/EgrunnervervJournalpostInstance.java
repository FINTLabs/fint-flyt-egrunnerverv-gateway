package no.fintlabs.instance.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class EgrunnervervJournalpostInstance {
    @NotNull
    @Valid
    private final EgrunnervervJournalpostInstanceBody egrunnervervJournalpostInstanceBody;
    @NotNull
    private final String saksnummer;
}
