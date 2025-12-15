package no.novari.flyt.egrunnerverv.gateway.slack;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SlackConfig {
    @Bean
    public WebClient slackWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}
