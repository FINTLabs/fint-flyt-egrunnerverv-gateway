package no.novari.flyt.egrunnerverv.gateway.instance;

import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static no.novari.flyt.egrunnerverv.gateway.ResourceLinkUtil.getFirstSelfLink;

@Repository
public class ResourceRepository {

    private final Map<String, PersonalressursResource> personalressursResources = new HashMap<>();
    private final Map<String, ArkivressursResource> arkivressursResources = new HashMap<>();


    public Optional<String> getArkivressursHrefFromPersonEmail(String epost) {

        Optional<PersonalressursResource> personalressursResource = Optional.ofNullable(personalressursResources.get(epost.toLowerCase()));

        return personalressursResource.flatMap(resource -> arkivressursResources
                .values()
                .stream()
                .filter(arkivressursResource -> filterArkivRessurs(arkivressursResource, resource.getBrukernavn().getIdentifikatorverdi()))
                .findFirst()
                .map(ResourceLinkUtil::getFirstSelfLink));
    }

    private boolean filterArkivRessurs(ArkivressursResource arkivressursResource, String personalRessursUsername) {
        return arkivressursResource.getPersonalressurs()
                .stream()
                .anyMatch(link -> StringUtils.endsWithIgnoreCase(link.getHref(), personalRessursUsername));
    }

    public void updatePersonalRessurs(PersonalressursResource resource) {
        if (resource.getKontaktinformasjon() != null && resource.getKontaktinformasjon().getEpostadresse() != null) {
            personalressursResources.put(resource.getKontaktinformasjon().getEpostadresse().toLowerCase(), resource);
        }
    }

    public void updateArkivRessurs(ArkivressursResource resource) {
        arkivressursResources.put(getFirstSelfLink(resource), resource);
    }

}
