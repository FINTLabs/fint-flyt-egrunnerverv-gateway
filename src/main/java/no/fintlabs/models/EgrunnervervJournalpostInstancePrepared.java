package no.fintlabs.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class EgrunnervervJournalpostInstancePrepared {
    private final String saksnummer;
    private final String sysId;
    private final String tittel;
    private final String dokumentNavn;
    private final String dokumentDato;
    private final String forsendelsesMate;
    private final String kommunenavn;
    private final String knr;
    private final String gnr;
    private final String bnr;
    private final String fnr;
    private final String snr;
    private final String eierforhold;
    private final String id;
    private final String maltittel;
    private final String prosjektnavn;
    private final String saksbehandlerEpost;
    @Setter
    private String saksbehandler;

    private final List<EgrunnervervJournalpostReceiver> mottakere;
    private final List<EgrunnervervJournalpostDocument> dokumenter;
}
