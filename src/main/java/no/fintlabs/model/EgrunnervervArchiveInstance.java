package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EgrunnervervArchiveInstance {

    @NotBlank
    private String sys_id;
    @NotNull
    private String knr;
    @NotNull
    private String gnr;
    @NotNull
    private String bnr;
    @NotNull
    private String fnr;
    @NotNull
    private String snr;
    @NotNull
    private String takstnummer;
    @NotNull
    private String tittel;
    @NotNull
    private String saksansvarligEpost;
    @NotNull
    private String eierforholdsnavn;
    @NotNull
    private String eierforholdskode;
    @NotNull
    private String prosjektnr;
    @NotNull
    private String prosjektnavn;
    @NotNull
    private String kommunenavn;
    @NotNull
    private String adresse;


    private List<@Valid @NotNull EgrunnervervArchiveCasePart> saksparter;
    private List<@Valid @NotNull EgrunnervervArchiveClassification> klasseringer;
}
