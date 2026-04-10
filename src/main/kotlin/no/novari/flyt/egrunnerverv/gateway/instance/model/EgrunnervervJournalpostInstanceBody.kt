package no.novari.flyt.egrunnerverv.gateway.instance.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class EgrunnervervJournalpostInstanceBody(
    @field:JsonProperty("sys_id")
    val sysId: String,
    @field:NotBlank
    val table: String,
    val tittel: String,
    val dokumentNavn: String? = null,
    val dokumentDato: String,
    @field:JsonProperty("forsendelsesmaate")
    val forsendelsesMate: String,
    val kommunenavn: String,
    val knr: String,
    val gnr: String,
    val bnr: String,
    val fnr: String,
    val snr: String,
    val eierforhold: String? = null,
    val id: String,
    val maltittel: String,
    val prosjektnavn: String,
    @field:JsonProperty("saksbehandler")
    val saksbehandlerEpost: String,
    val mottakere: List<@Valid EgrunnervervJournalpostReceiver> = emptyList(),
    val dokumenter: List<@Valid EgrunnervervJournalpostDocument> = emptyList(),
)
