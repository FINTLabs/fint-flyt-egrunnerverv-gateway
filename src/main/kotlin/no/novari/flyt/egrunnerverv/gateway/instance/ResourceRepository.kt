package no.novari.flyt.egrunnerverv.gateway.instance

import no.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil.getFirstSelfLink
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils

@Repository
class ResourceRepository {
    private val personalressursResources: MutableMap<String, PersonalressursResource> = HashMap()
    private val arkivressursResources: MutableMap<String, ArkivressursResource> = HashMap()

    fun getArkivressursHrefFromPersonEmail(epost: String): String? {
        val personalressursResource = personalressursResources[epost.lowercase()] ?: return null
        return arkivressursResources.values
            .firstOrNull { arkivressursResource ->
                filterArkivRessurs(arkivressursResource, personalressursResource.brukernavn.identifikatorverdi)
            }?.let(ResourceLinkUtil::getFirstSelfLink)
    }

    private fun filterArkivRessurs(
        arkivressursResource: ArkivressursResource,
        personalRessursUsername: String,
    ): Boolean =
        arkivressursResource.personalressurs
            .any { link -> StringUtils.endsWithIgnoreCase(link.href, personalRessursUsername) }

    fun updatePersonalRessurs(resource: PersonalressursResource) {
        val epost = resource.kontaktinformasjon?.epostadresse
        if (epost != null) {
            personalressursResources[epost.lowercase()] = resource
        }
    }

    fun updateArkivRessurs(resource: ArkivressursResource) {
        arkivressursResources[getFirstSelfLink(resource)] = resource
    }
}
