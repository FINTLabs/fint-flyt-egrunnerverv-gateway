package no.novari.flyt.egrunnerverv.gateway.instance

import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils
import java.util.concurrent.ConcurrentHashMap

@Repository
class ResourceRepository {
    private val personalressursResources = ConcurrentHashMap<String, PersonalressursResource>()
    private val arkivressursResources = ConcurrentHashMap<String, ArkivressursResource>()

    fun getArkivressursHrefFromPersonEmail(epost: String): String? {
        val personalressursResource = personalressursResources[epost.lowercase()] ?: return null

        return arkivressursResources.values
            .firstOrNull { filterArkivRessurs(it, personalressursResource.brukernavn.identifikatorverdi) }
            ?.let(ResourceLinkUtil::getFirstSelfLink)
    }

    fun updatePersonalRessurs(resource: PersonalressursResource) {
        val email = resource.kontaktinformasjon?.epostadresse?.lowercase() ?: return
        personalressursResources[email] = resource
    }

    fun updateArkivRessurs(resource: ArkivressursResource) {
        arkivressursResources[ResourceLinkUtil.getFirstSelfLink(resource)] = resource
    }

    private fun filterArkivRessurs(
        arkivressursResource: ArkivressursResource,
        personalRessursUsername: String,
    ): Boolean =
        arkivressursResource.personalressurs
            .any { StringUtils.endsWithIgnoreCase(it.href, personalRessursUsername) }
}
