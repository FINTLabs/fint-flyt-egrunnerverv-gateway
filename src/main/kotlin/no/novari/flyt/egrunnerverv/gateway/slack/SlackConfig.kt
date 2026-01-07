package no.novari.flyt.egrunnerverv.gateway.slack

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SlackConfig {
    @Bean
    fun slackRestClient(builder: RestClient.Builder): RestClient = builder.build()
}
