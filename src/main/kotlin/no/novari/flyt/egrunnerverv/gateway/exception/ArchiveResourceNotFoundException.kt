package no.novari.flyt.egrunnerverv.gateway.exception

import no.novari.flyt.egrunnerverv.gateway.slack.SlackAlertService
import no.novari.flyt.gateway.webinstance.exception.AbstractInstanceRejectedException

class ArchiveResourceNotFoundException(
    email: String,
    slackAlertService: SlackAlertService,
) : AbstractInstanceRejectedException(formatMessage(email)) {
    init {
        slackAlertService.sendMessage(formatMessage(email))
    }

    companion object {
        private const val MESSAGE_TEMPLATE = "No archive resource found for saksansvarligEpost='%s'"

        private fun formatMessage(email: String): String = MESSAGE_TEMPLATE.format(email)
    }
}
