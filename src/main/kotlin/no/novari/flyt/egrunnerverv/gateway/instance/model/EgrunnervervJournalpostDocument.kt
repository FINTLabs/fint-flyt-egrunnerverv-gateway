package no.novari.flyt.egrunnerverv.gateway.instance.model

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class EgrunnervervJournalpostDocument(
    @field:NotNull
    val tittel: String,
    @field:NotNull
    val hoveddokument: Boolean,
    @field:NotNull
    val filnavn: String,
    @field:NotEmpty
    val dokumentBase64: String,
)
