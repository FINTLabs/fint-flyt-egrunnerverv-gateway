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
public class EgrunnervervSakInstancePrepared {

    private final String sysId;
    private final String knr;
    private final String gnr;
    private final String bnr;
    private final String fnr;
    private final String snr;
    private final String takstnummer;
    private final String tittel;
    private final String saksansvarligEpost;
    @Setter
    private String saksansvarlig;
    private final String eierforholdsnavn;
    private final String eierforholdskode;
    private final String prosjektnr;
    private final String prosjektnavn;
    private final String kommunenavn;
    private final String adresse;

    private final List<EgrunnervervSaksPart> saksparter;
    private final List<EgrunnervervSakKlassering> klasseringer;
}
