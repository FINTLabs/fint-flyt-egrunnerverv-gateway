package no.novari.flyt.egrunnerverv.gateway.instance;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.novari.cache.FintCache;
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
public class ResourceRepository {

    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final FintCache<String, ArkivressursResource> arkivressursResourceCache;

    public ResourceRepository(
            FintCache<String, PersonalressursResource> personalressursResourceCache,
            FintCache<String, ArkivressursResource> arkivressursResourceCache
    ) {
        this.personalressursResourceCache = personalressursResourceCache;
        this.arkivressursResourceCache = arkivressursResourceCache;
    }

    public Optional<String> getArkivressursHrefFromPersonEmail(String epost) {
        String normalizedEmail = Optional.ofNullable(epost)
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");

        Optional<String> personalRessursUsername = personalressursResourceCache.getAllDistinct()
                .stream()
                .filter(resource -> StringUtils.hasText(getEmailAddress(resource)))
                .filter(resource -> normalizedEmail.equals(getEmailAddress(resource).toLowerCase()))
                .map(this::getUsername)
                .filter(StringUtils::hasText)
                .findFirst();

        return personalRessursUsername.flatMap(username -> arkivressursResourceCache.getAllDistinct()
                .stream()
                .filter(arkivressursResource -> filterArkivRessurs(arkivressursResource, username))
                .findFirst()
                .map(ResourceLinkUtil::getFirstSelfLink));
    }

    private boolean filterArkivRessurs(ArkivressursResource arkivressursResource, String personalRessursUsername) {
        return Optional.ofNullable(arkivressursResource.getPersonalressurs())
                .orElse(List.of())
                .stream()
                .anyMatch(link -> StringUtils.endsWithIgnoreCase(link.getHref(), personalRessursUsername));
    }

    private String getEmailAddress(PersonalressursResource resource) {
        return Optional.ofNullable(resource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .orElse(null);
    }

    private String getUsername(PersonalressursResource resource) {
        return Optional.ofNullable(resource.getBrukernavn())
                .map(Identifikator::getIdentifikatorverdi)
                .orElse(null);
    }
}
