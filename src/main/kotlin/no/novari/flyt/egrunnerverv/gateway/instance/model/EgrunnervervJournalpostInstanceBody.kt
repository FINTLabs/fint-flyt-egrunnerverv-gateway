package no.novari.flyt.egrunnerverv.gateway.instance.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EgrunnervervJournalpostInstanceBody(
    @field:NotNull
    @field:JsonProperty("sys_id")
    val sysId: String,
    @field:NotBlank
    val table: String,
    @field:NotNull
    val tittel: String,
    val dokumentNavn: String? = null,
    @field:NotNull
    val dokumentDato: String,
    @field:NotNull
    @field:JsonProperty("forsendelsesmaate")
    val forsendelsesMate: String,
    @field:NotNull
    val kommunenavn: String,
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
    val eierforhold: String? = null,
    @field:NotNull
    val id: String,
    @field:NotNull
    val maltittel: String,
    @field:NotNull
    val prosjektnavn: String,
    @field:NotNull
    @field:JsonProperty("saksbehandler")
    val saksbehandlerEpost: String,
    @field:Valid
    val mottakere: List<EgrunnervervJournalpostReceiver>,
    @field:Valid
    val dokumenter: List<EgrunnervervJournalpostDocument>,
)
