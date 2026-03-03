package com.mfcs.micro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        ReactiveOAuth2ClientAutoConfiguration.class
})
class MfcsMicroApplicationTests {

    @MockBean
    private ReactiveClientRegistrationRepository reactiveClientRegistrationRepository;

    @MockBean
    private WebClient mfcsWebClient;

    @Test
    void contextLoads() {
    }
}
