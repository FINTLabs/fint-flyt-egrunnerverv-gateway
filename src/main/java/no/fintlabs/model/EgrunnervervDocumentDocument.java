package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EgrunnervervDocumentDocument {
    private String tittel;
    private String hoveddokument;
    private String filnavn;
    private String dokumentBase64;
}
