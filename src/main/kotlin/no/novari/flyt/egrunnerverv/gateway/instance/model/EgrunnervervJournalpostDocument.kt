package no.novari.flyt.egrunnerverv.gateway.instance.model

import jakarta.validation.constraints.Size

data class EgrunnervervJournalpostDocument(
    val tittel: String,
    val hoveddokument: Boolean,
    val filnavn: String,
    @field:Size(min = 1)
    val dokumentBase64: ByteArray,
)
