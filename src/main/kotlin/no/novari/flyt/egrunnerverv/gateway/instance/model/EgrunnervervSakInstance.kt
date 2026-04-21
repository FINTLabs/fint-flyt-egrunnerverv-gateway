package no.novari.flyt.egrunnerverv.gateway.instance.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class EgrunnervervSakInstance(
    @field:NotBlank
    @field:JsonProperty("sys_id")
    val sysId: String,
    @field:NotBlank
    val table: String,
    val knr: String,
    val gnr: String,
    val bnr: String,
    val fnr: String,
    val snr: String,
    val takstnummer: String,
    val tittel: String,
    val saksansvarligEpost: String,
    val eierforholdsnavn: String,
    val eierforholdskode: String,
    val prosjektnr: String,
    val prosjektnavn: String,
    val kommunenavn: String,
    val adresse: String,
    val saksparter: List<@Valid EgrunnervervSaksPart> = emptyList(),
    val klasseringer: List<@Valid EgrunnervervSakKlassering> = emptyList(),
)
