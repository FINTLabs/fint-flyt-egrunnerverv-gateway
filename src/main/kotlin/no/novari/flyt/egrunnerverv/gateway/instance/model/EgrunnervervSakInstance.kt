package no.novari.flyt.egrunnerverv.gateway.instance.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EgrunnervervSakInstance(
    @field:NotBlank
    @field:JsonProperty("sys_id")
    val sysId: String,
    @field:NotBlank
    val table: String,
    @field:NotNull
    val knr: String,
    @field:NotNull
    val gnr: String,
    @field:NotNull
    val bnr: String,
    @field:NotNull
    val fnr: String,
    @field:NotNull
    val snr: String,
    @field:NotNull
    val takstnummer: String,
    @field:NotNull
    val tittel: String,
    @field:NotNull
    val saksansvarligEpost: String,
    @field:NotNull
    val eierforholdsnavn: String,
    @field:NotNull
    val eierforholdskode: String,
    @field:NotNull
    val prosjektnr: String,
    @field:NotNull
    val prosjektnavn: String,
    @field:NotNull
    val kommunenavn: String,
    @field:NotNull
    val adresse: String,
    @field:Valid
    val saksparter: List<EgrunnervervSaksPart>,
    @field:Valid
    val klasseringer: List<EgrunnervervSakKlassering>,
)
