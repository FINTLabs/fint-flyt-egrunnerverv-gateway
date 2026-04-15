package no.novari.flyt.egrunnerverv.gateway.exception

import no.novari.fint.model.resource.FintLinks

class NoSuchLinkException(
    message: String,
) : RuntimeException(message) {
    companion object {
        fun noSelfLink(resource: FintLinks): NoSuchLinkException =
            NoSuchLinkException("No self link in resource=$resource")
    }
}
