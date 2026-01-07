package no.novari.flyt.egrunnerverv.gateway.instance.model

import jakarta.validation.constraints.NotNull

data class EgrunnervervSakKlassering(
    @field:NotNull
    val ordningsprinsipp: String,
    @field:NotNull
    val ordningsverdi: String,
    @field:NotNull
    val beskrivelse: String,
    @field:NotNull
    val sortering: String,
    @field:NotNull
    val untattOffentlighet: String,
)
