package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EgrunnervervArchiveInstance {

    @NotBlank
    private String sysId;
    private String knr;
    private String gnr;
    private String bnr;
    private String fnr;
    private String snr;
    private String takstnummer;
    private String tittel;
    private String saksansvarligEpost;

    private List<EgrunnervervArchiveCasePart> saksparter;
    private List<EgrunnervervArchiveClassification> klasseringer;
}
