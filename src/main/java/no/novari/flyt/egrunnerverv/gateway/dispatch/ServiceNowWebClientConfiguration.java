package no.novari.flyt.egrunnerverv.gateway.dispatch;

import io.netty.channel.ChannelOption;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fint.flyt.egrunnerverv.dispatch")
public class ServiceNowWebClientConfiguration {

    public static final int UNLIMITED = -1;
    private String baseUrl;
    private String username;
    private String password;
    private String registrationId;

    private static final int MAX_CONNECTIONS = 25;
    public static final int NO_UPPER_LIMIT = -1;
    private static final Duration ACQUIRE_TIMEOUT = Duration.ofMinutes(15);
    private static final int CONNECT_TIMEOUT_MILLIS = 900_000;
    private static final Duration RESPONSE_TIMEOUT = Duration.ofMinutes(10);
    public static final Duration MAX_LIFE_TIME = Duration.ofMinutes(30);
    public static final Duration MAX_IDLE_TIME = Duration.ofMinutes(5);

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository, ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder().password().refreshToken().build();

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        authorizedClientManager.setContextAttributesMapper(oAuth2AuthorizeRequest -> Mono.just(Map.of(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username, OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password)));

        return authorizedClientManager;
    }

    @Bean(name = "dispatchClientHttpConnector")
    public ClientHttpConnector dispatchClientHttpConnector() {
        return new ReactorClientHttpConnector(
                HttpClient.create(
                                ConnectionProvider.builder("laidback")
                                        .maxConnections(MAX_CONNECTIONS)
                                        .pendingAcquireMaxCount(NO_UPPER_LIMIT)
                                        .pendingAcquireTimeout(ACQUIRE_TIMEOUT)
                                        .maxLifeTime(MAX_LIFE_TIME)
                                        .maxIdleTime(MAX_IDLE_TIME)
                                        .build())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                        .responseTimeout(RESPONSE_TIMEOUT)
        );
    }

    @Bean
    public WebClient webClient(
            WebClient.Builder builder,
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
            @Qualifier("dispatchClientHttpConnector") ClientHttpConnector clientHttpConnector) {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(UNLIMITED))
                .build();

        var oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId(registrationId);

        return builder
                .clientConnector(clientHttpConnector)
                .exchangeStrategies(exchangeStrategies)
                .filter(oauth2Client)
                .baseUrl(baseUrl)
                .build();
    }


}

