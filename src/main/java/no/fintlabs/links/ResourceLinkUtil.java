package no.fintlabs.links;

import no.fint.model.resource.FintLinks;

public class ResourceLinkUtil {

    public static String getFirstSelfLink(FintLinks resource) {
        return resource.getSelfLinks()
                .stream()
                .findFirst()
                .orElseThrow(() -> NoSuchLinkException.noSelfLink(resource))
                .getHref();
    }
}
