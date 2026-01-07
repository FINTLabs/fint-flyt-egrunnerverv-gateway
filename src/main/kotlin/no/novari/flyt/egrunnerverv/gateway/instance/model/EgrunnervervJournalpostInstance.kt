package no.novari.flyt.egrunnerverv.gateway.instance.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class EgrunnervervJournalpostInstance(
    @field:NotNull
    @field:Valid
    val egrunnervervJournalpostInstanceBody: EgrunnervervJournalpostInstanceBody,
    @field:NotNull
    val saksnummer: String,
)
