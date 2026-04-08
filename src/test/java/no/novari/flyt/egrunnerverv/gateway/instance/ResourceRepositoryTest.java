package no.novari.flyt.egrunnerverv.gateway.instance;

import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.novari.cache.FintCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceRepositoryTest {

    private FintCache<String, PersonalressursResource> personalressursResourceCache;
    private FintCache<String, ArkivressursResource> arkivressursResourceCache;
    private ResourceRepository resourceRepository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        personalressursResourceCache = mock(FintCache.class);
        arkivressursResourceCache = mock(FintCache.class);
        resourceRepository = new ResourceRepository(personalressursResourceCache, arkivressursResourceCache);
    }

    @Test
    void shouldResolveArkivressursFromEmail() {
        PersonalressursResource personalressursResource = mock(PersonalressursResource.class, RETURNS_DEEP_STUBS);
        when(personalressursResource.getKontaktinformasjon().getEpostadresse()).thenReturn("Person@Novari.no");
        when(personalressursResource.getBrukernavn().getIdentifikatorverdi()).thenReturn("user-1");

        ArkivressursResource arkivressursResource = mock(ArkivressursResource.class);
        Link personalLink = mock(Link.class);
        when(personalLink.getHref()).thenReturn("https://api/personalressurs/user-1");
        when(arkivressursResource.getPersonalressurs()).thenReturn(List.of(personalLink));

        Link selfLink = mock(Link.class);
        when(selfLink.getHref()).thenReturn("https://api/arkivressurs/123");
        when(arkivressursResource.getSelfLinks()).thenReturn(List.of(selfLink));

        when(personalressursResourceCache.getAllDistinct()).thenReturn(List.of(personalressursResource));
        when(arkivressursResourceCache.getAllDistinct()).thenReturn(List.of(arkivressursResource));

        Optional<String> result = resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no");

        assertThat(result).contains("https://api/arkivressurs/123");
    }

    @Test
    void shouldReturnEmptyWhenNoMatchingPersonalressurs() {
        when(personalressursResourceCache.getAllDistinct()).thenReturn(List.of());
        when(arkivressursResourceCache.getAllDistinct()).thenReturn(List.of());

        Optional<String> result = resourceRepository.getArkivressursHrefFromPersonEmail("person@novari.no");

        assertThat(result).isEmpty();
    }
}
