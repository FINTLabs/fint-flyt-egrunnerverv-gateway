package no.fintlabs.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class EgrunnervervJournalpostInstancePrepared {
    @NotNull
    private final String saksnummer;
    @NotNull
    private final String sysId;
    @NotNull
    private final String tittel;
    @NotNull
    private final String dokumentNavn;
    @NotNull
    private final String dokumentDato;
    @NotNull
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
    private final String eierforhold;
    @NotNull
    private final String id;
    @NotNull
    private final String maltittel;
    @NotNull
    private final String prosjektnavn;
    @NotNull
    private final String saksbehandlerEpost;
    @NotNull
    @Setter
    private String saksbehandler;

    private final List<@Valid @NotNull EgrunnervervJournalpostReceiver> mottakere;
    private final List<@Valid @NotNull EgrunnervervJournalpostDocument> dokumenter;
}
