package no.novari.flyt.egrunnerverv.gateway.instance

import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstance
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervJournalpostInstanceBody
import no.novari.flyt.egrunnerverv.gateway.instance.model.EgrunnervervSakInstance
import no.novari.flyt.gateway.webinstance.InstanceProcessor
import no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("{$EXTERNAL_API}/api/egrunnerverv/instances/{orgNr}")
class EgrunnervervInstanceController(
    private val sakInstanceProcessor: InstanceProcessor<EgrunnervervSakInstance>,
    private val journalpostInstanceProcessor: InstanceProcessor<EgrunnervervJournalpostInstance>,
) {
    @PostMapping("archive")
    fun postSakInstance(
        @RequestBody egrunnervervSakInstance: EgrunnervervSakInstance,
        authentication: Authentication,
    ): ResponseEntity<*> = sakInstanceProcessor.processInstance(authentication, egrunnervervSakInstance)

    @PostMapping("document")
    fun postJournalpostInstance(
        @RequestBody egrunnervervJournalpostInstanceBody: EgrunnervervJournalpostInstanceBody,
        @RequestParam("id") saksnummer: String,
        authentication: Authentication,
    ): ResponseEntity<*> {
        val egrunnervervJournalpostInstance =
            EgrunnervervJournalpostInstance(
                egrunnervervJournalpostInstanceBody = egrunnervervJournalpostInstanceBody,
                saksnummer = saksnummer,
            )

        return journalpostInstanceProcessor.processInstance(authentication, egrunnervervJournalpostInstance)
    }
}
