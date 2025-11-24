package no.novari.flyt.egrunnerverv.gateway.instance.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
