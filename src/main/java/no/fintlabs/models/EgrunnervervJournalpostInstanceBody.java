package no.fintlabs.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class EgrunnervervJournalpostInstanceBody {
    @NotNull
    @JsonProperty("sys_id")
    private final String sysId;
    @NotBlank
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
    @NotNull
    private final String kommunenavn;
    @NotNull
    private final String knr;
    @NotNull
    private final String gnr;
    @NotNull
    private final String bnr;
    @NotNull
    private final String fnr;
    @NotNull
    private final String snr;
    @NotNull
    private final String eierforholdskode;
    @NotNull
    private final String prosjektnavn;
    @NotNull
    @JsonProperty("saksbehandler")
    private final String saksbehandlerEpost;

    private final List<@Valid @NotNull EgrunnervervJournalpostReceiver> mottakere;
    private final List<@Valid @NotNull EgrunnervervJournalpostDocument> dokumenter;
}
