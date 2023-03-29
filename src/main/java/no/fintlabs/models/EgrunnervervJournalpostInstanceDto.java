package no.fintlabs.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class EgrunnervervJournalpostInstanceDto {
    @NotNull
    @JsonProperty("sys_id")
    private final String sysId;
    @NotNull
    private final String table;
    @NotNull
    private final String tittel;
    @NotNull
    private final String dokumentNavn;
    @NotNull
    private final String dokumentDato;
    @NotNull
    @JsonProperty("forsendelsesmaate")
    private final String forsendelsesMate;

    private final List<@Valid @NotNull EgrunnervervJournalpostReceiver> mottakere;
    private final List<@Valid @NotNull EgrunnervervJournalpostDocument> dokumenter;
}
