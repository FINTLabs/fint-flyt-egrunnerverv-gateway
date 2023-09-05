package no.fintlabs;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.models.EgrunnervervJournalpostInstance;
import no.fintlabs.models.EgrunnervervJournalpostInstanceBody;
import no.fintlabs.models.EgrunnervervSakInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class EgrunnervervInstanceControllerTest {

    @InjectMocks
    private EgrunnervervInstanceController controller;

    @Mock
    private InstanceProcessor<EgrunnervervSakInstance> sakInstanceProcessor;

    @Mock
    private InstanceProcessor<EgrunnervervJournalpostInstance> journalpostInstanceProcessor;

    @Mock
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPostSakInstance() {
        EgrunnervervSakInstance sakInstance = EgrunnervervSakInstance.builder().build();

        when(sakInstanceProcessor.processInstance(any(), any())).thenReturn(Mono.just(new ResponseEntity<>(HttpStatus.OK)));

        StepVerifier.create(
                        controller.postSakInstance(sakInstance, Mono.just(mockAuthentication))
                )
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testPostJournalpostInstance() {
        EgrunnervervJournalpostInstanceBody journalpostInstanceBody = EgrunnervervJournalpostInstanceBody.builder().build();
        when(journalpostInstanceProcessor.processInstance(any(), any())).thenReturn(Mono.just(new ResponseEntity<>(HttpStatus.OK)));

        StepVerifier.create(
                        controller.postJournalpostInstance(journalpostInstanceBody, "someId", Mono.just(mockAuthentication))
                )
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

}
