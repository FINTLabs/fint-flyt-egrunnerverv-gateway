package no.novari.flyt.egrunnerverv.gateway.dispatch

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.client.RestClient

@Configuration
@ConfigurationProperties(prefix = "novari.flyt.egrunnerverv.dispatch")
class ServiceNowWebClientConfiguration {
    var baseUrl: String = ""
    var username: String = ""
    var password: String = ""
    var registrationId: String = ""

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .password()
                .refreshToken()
                .build()

        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            )

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        authorizedClientManager.setContextAttributesMapper {
            mapOf(
                OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME to username,
                OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME to password,
            )
        }

        return authorizedClientManager
    }

    @Bean(name = ["dispatchRestClient"])
    fun restClient(
        builder: RestClient.Builder,
        authorizedClientManager: OAuth2AuthorizedClientManager,
    ): RestClient {
        val principal =
            AnonymousAuthenticationToken(
                "system",
                "system",
                AuthorityUtils.createAuthorityList("ROLE_SYSTEM"),
            )

        val oauth2Interceptor =
            ClientHttpRequestInterceptor { request, body, execution ->
                val authorizeRequest =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId(registrationId)
                        .principal(principal)
                        .build()
                val authorizedClient =
                    authorizedClientManager.authorize(authorizeRequest)
                        ?: throw IllegalStateException("OAuth2 authorization failed for $registrationId")
                request.headers.setBearerAuth(authorizedClient.accessToken.tokenValue)
                execution.execute(request, body)
            }

        return builder
            .baseUrl(baseUrl)
            .requestInterceptor(oauth2Interceptor)
            .build()
    }
}
