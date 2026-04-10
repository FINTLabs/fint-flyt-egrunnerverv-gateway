package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model

import com.fasterxml.jackson.annotation.JsonProperty

data class JournalpostReceipt(
    val journalpostnr: String,
    val tittel: String?,
    val statusId: String?,
    val tilgangskode: String?,
    val hjemmel: String?,
    val dokumentdato: String?,
    val dokumenttypeid: String?,
    val dokumenttypenavn: String?,
    val saksansvarligbrukernavn: String?,
    val saksansvarlignavn: String?,
    val adminenhetkortnavn: String?,
    val adminenhetnavn: String?,
)

data class SakReceipt(
    val arkivnummer: String,
    @field:JsonProperty("opprettelse_i_elements_fullfort")
    val opprettelseIElementsFullfort: String?,
)
