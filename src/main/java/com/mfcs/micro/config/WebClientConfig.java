package com.mfcs.micro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;

/**
 * WebClient configuration with OAuth2 client credentials support
 * for calling the Oracle MFCS REST API.
 */
@Configuration
public class WebClientConfig {

    @Value("${mfcs.api.base-url}")
    private String mfcsApiBaseUrl;

    @Value("${mfcs.api.timeout-seconds:30}")
    private int timeoutSeconds;

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        InMemoryReactiveOAuth2AuthorizedClientService clientService =
                new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, clientService);
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }

    @Bean
    public WebClient mfcsWebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Filter.setDefaultClientRegistrationId("mfcs-api");

        return WebClient.builder()
                .baseUrl(mfcsApiBaseUrl)
                .filter(oauth2Filter)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024))
                .build();
    }
}
