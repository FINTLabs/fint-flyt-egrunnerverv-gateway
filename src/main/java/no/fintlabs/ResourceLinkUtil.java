package no.fintlabs;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fintlabs.exception.NoSuchLinkException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceLinkUtil {

    public static String getFirstSelfLink(FintLinks resource) {
        return resource.getSelfLinks()
                .stream()
                .findFirst()
                .orElseThrow(() -> NoSuchLinkException.noSelfLink(resource))
                .getHref();
    }

    public static List<String> getSelfLinks(FintLinks resource) {
        return resource.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .collect(Collectors.toList());
    }

    public static Optional<String> getOptionalFirstLink(Supplier<List<Link>> linkProducer) {
        return Optional.ofNullable(linkProducer.get())
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .map(Link::getHref);
    }

}
