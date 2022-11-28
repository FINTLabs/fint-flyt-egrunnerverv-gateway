package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EgrunnervervDocumentInstance {

    private String tittel;
    private String dokumentDato;
    private String dokumentTypeId;
    private String dokumentkategoriId;
    private String tilgangskode;
    private String hjemmel;
    private String merknad;
    private String avskrivDirekte;
    private String forsendelsesmaate;
    private String avsender;

    private List<EgrunnervervDocumentDocument> dokumenter;

}
