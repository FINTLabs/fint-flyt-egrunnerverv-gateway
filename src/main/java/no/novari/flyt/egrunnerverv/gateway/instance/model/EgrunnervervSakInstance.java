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
@Builder(toBuilder = true)
public class EgrunnervervSakInstance {

    @NotBlank
    @JsonProperty("sys_id")
    private final String sysId;
    @NotBlank
    private final String table;
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
    private final String takstnummer;
    @NotNull
    private final String tittel;
    @NotNull
    private final String saksansvarligEpost;
    @NotNull
    private final String eierforholdsnavn;
    @NotNull
    private final String eierforholdskode;
    @NotNull
    private final String prosjektnr;
    @NotNull
    private final String prosjektnavn;
    @NotNull
    private final String kommunenavn;
    @NotNull
    private final String adresse;

    private final List<@Valid @NotNull EgrunnervervSaksPart> saksparter;
    private final List<@Valid @NotNull EgrunnervervSakKlassering> klasseringer;
}
