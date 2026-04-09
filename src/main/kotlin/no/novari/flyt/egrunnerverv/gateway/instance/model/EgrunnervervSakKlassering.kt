package no.novari.flyt.egrunnerverv.gateway.instance.model

data class EgrunnervervSakKlassering(
    val ordningsprinsipp: String,
    val ordningsverdi: String,
    val beskrivelse: String,
    val sortering: String,
    val untattOffentlighet: String,
)
