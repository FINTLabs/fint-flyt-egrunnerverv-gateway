package no.novari.flyt.egrunnerverv.gateway.dispatch.model

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
