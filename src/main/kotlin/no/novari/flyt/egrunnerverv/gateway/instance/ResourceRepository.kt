package no.novari.flyt.egrunnerverv.gateway.instance

import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.flyt.cache.FlytCache
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils

@Repository
class ResourceRepository(
    private val personalressursResourceCache: FlytCache<String, PersonalressursResource>,
    private val arkivressursResourceCache: FlytCache<String, ArkivressursResource>,
) {
    fun getArkivressursHrefFromPersonEmail(epost: String): String? {
        val normalizedEmail = epost.trim().lowercase()
        if (normalizedEmail.isBlank()) {
            return null
        }

        val personalRessursUsername =
            personalressursResourceCache
                .distinctValues()
                .asSequence()
                .filter { getEmailAddress(it)?.trim()?.lowercase() == normalizedEmail }
                .mapNotNull(::getUsername)
                .map(String::trim)
                .firstOrNull { it.isNotBlank() }
                ?: return null

        return arkivressursResourceCache
            .distinctValues()
            .asSequence()
            .firstOrNull { filterArkivRessurs(it, personalRessursUsername) }
            ?.let(ResourceLinkUtil::getFirstSelfLink)
    }

    private fun filterArkivRessurs(
        arkivressursResource: ArkivressursResource,
        personalRessursUsername: String,
    ): Boolean =
        arkivressursResource.personalressurs
            .orEmpty()
            .any { StringUtils.endsWithIgnoreCase(it.href, personalRessursUsername) }

    private fun getEmailAddress(resource: PersonalressursResource): String? = resource.kontaktinformasjon?.epostadresse

    private fun getUsername(resource: PersonalressursResource): String? = resource.brukernavn?.identifikatorverdi
}
