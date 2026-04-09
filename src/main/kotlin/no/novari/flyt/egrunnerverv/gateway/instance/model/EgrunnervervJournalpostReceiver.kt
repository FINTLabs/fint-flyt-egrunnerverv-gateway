package no.novari.flyt.egrunnerverv.gateway.instance.model

data class EgrunnervervJournalpostReceiver(
    val navn: String,
    val organisasjonsnummer: String,
    val epost: String,
    val telefon: String,
    val postadresse: String,
    val postnummer: String,
    val poststed: String,
)
