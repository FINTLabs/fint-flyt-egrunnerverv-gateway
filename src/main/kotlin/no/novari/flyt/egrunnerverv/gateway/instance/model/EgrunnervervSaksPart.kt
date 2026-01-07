package no.novari.flyt.egrunnerverv.gateway.instance.model

import jakarta.validation.constraints.NotNull

data class EgrunnervervSaksPart(
    @field:NotNull
    val navn: String,
    @field:NotNull
    val organisasjonsnummer: String,
    @field:NotNull
    val epost: String,
    @field:NotNull
    val telefon: String,
    @field:NotNull
    val postadresse: String,
    @field:NotNull
    val postnummer: String,
    @field:NotNull
    val poststed: String,
)
