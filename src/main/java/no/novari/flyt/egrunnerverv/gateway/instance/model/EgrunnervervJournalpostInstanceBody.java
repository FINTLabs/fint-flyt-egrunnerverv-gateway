package no.novari.flyt.egrunnerverv.gateway.instance.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
    //    @NotNull
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
    //    @NotNull
    private final String eierforhold;
    @NotNull
    private final String id;
    @NotNull
    private final String maltittel;
    @NotNull
    private final String prosjektnavn;
    @NotNull
    @JsonProperty("saksbehandler")
    private final String saksbehandlerEpost;

    private final List<@Valid @NotNull EgrunnervervJournalpostReceiver> mottakere;
    private final List<@Valid @NotNull EgrunnervervJournalpostDocument> dokumenter;
}
