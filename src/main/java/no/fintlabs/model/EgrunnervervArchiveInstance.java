package no.fintlabs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EgrunnervervArchiveInstance {

    @NotBlank
    @JsonProperty("sys_id")
    private String sysId;
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
