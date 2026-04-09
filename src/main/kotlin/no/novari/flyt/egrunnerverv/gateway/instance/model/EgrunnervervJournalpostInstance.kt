package no.novari.flyt.egrunnerverv.gateway.instance.model

import jakarta.validation.Valid

data class EgrunnervervJournalpostInstance(
    @field:Valid
    val egrunnervervJournalpostInstanceBody: EgrunnervervJournalpostInstanceBody,
    val saksnummer: String,
)
