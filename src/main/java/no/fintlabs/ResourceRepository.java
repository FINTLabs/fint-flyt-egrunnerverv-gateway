package no.fintlabs;

import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.fintlabs.links.ResourceLinkUtil;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ResourceRepository {

    private final Map<String, PersonalressursResource> personalressursResources = new HashMap<>();
    private final Map<String, ArkivressursResource> arkivressursResources = new HashMap<>();


    public Optional<String> getUsername(String epost) {

        Optional<PersonalressursResource> personalressursResource = Optional.ofNullable(personalressursResources.get(epost));

        return personalressursResource.flatMap(resource -> arkivressursResources
                .values()
                .stream()
                .filter(arkivressursResource -> filterArkivRessurs(arkivressursResource, resource.getBrukernavn().getIdentifikatorverdi()))
                .findFirst()
                .map(arkivressursResource -> arkivressursResource.getSystemId().getIdentifikatorverdi()));
    }

    private boolean filterArkivRessurs(ArkivressursResource arkivressursResource, String personalRessursUsername) {
        return arkivressursResource.getPersonalressurs()
                .stream()
                .anyMatch(link -> StringUtils.endsWithIgnoreCase(link.getHref(), personalRessursUsername));
    }

    public void updatePersonalRessurs(PersonalressursResource resource) {
        personalressursResources.put(resource.getKontaktinformasjon().getEpostadresse(), resource);
    }

    public void updateArkivRessurs(ArkivressursResource resource) {
        arkivressursResources.put(ResourceLinkUtil.getFirstSelfLink(resource), resource);
    }

}
