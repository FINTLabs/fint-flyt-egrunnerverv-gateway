package no.novari.flyt.egrunnerverv.gateway.instance.model

import jakarta.validation.constraints.NotEmpty

data class EgrunnervervJournalpostDocument(
    val tittel: String,
    val hoveddokument: Boolean,
    val filnavn: String,
    @field:NotEmpty
    val dokumentBase64: String,
)
