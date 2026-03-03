package com.mfcs.micro.service;

import com.mfcs.micro.exception.MfcsApiException;
import com.mfcs.micro.model.MfcsItemRequest;
import com.mfcs.micro.model.MfcsItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Service responsible for calling the Oracle MFCS REST API
 * with OAuth2 client credentials authentication.
 */
@Service
public class MfcsApiService {

    private static final Logger log = LoggerFactory.getLogger(MfcsApiService.class);

    private final WebClient mfcsWebClient;
    private final String itemEndpoint;
    private final int timeoutSeconds;

    public MfcsApiService(WebClient mfcsWebClient,
                          @Value("${mfcs.api.item-endpoint:/items}") String itemEndpoint,
                          @Value("${mfcs.api.timeout-seconds:30}") int timeoutSeconds) {
        this.mfcsWebClient = mfcsWebClient;
        this.itemEndpoint = itemEndpoint;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Sends an item creation request to the Oracle MFCS API.
     * The request is authenticated using OAuth2 client credentials flow.
     *
     * @param request the item creation payload
     * @return the response from MFCS API
     */
    public MfcsItemResponse createItem(MfcsItemRequest request) {
        log.info("Calling MFCS API to create item: itemId={}, itemCode={}",
                request.getItemId(), request.getItemCode());

        try {
            MfcsItemResponse response = mfcsWebClient.post()
                    .uri(itemEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatus.BAD_REQUEST::equals,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new MfcsApiException("MFCS API returned 400 Bad Request: " + body, 400))))
                    .onStatus(HttpStatus.UNAUTHORIZED::equals,
                            clientResponse -> Mono.error(
                                    new MfcsApiException("MFCS API returned 401 Unauthorized", 401)))
                    .onStatus(HttpStatus.FORBIDDEN::equals,
                            clientResponse -> Mono.error(
                                    new MfcsApiException("MFCS API returned 403 Forbidden", 403)))
                    .onStatus(status -> status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new MfcsApiException("MFCS API server error: " + body,
                                                    clientResponse.statusCode().value()))))
                    .bodyToMono(MfcsItemResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            log.info("MFCS API item creation successful: itemId={}, mfcsRef={}",
                    request.getItemId(),
                    response != null ? response.getMfcsReferenceId() : null);
            return response;

        } catch (WebClientResponseException ex) {
            log.error("MFCS API call failed for itemId={}: status={}, body={}",
                    request.getItemId(), ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MfcsApiException(
                    "MFCS API call failed with status " + ex.getStatusCode(), ex);
        } catch (MfcsApiException ex) {
            log.error("MFCS API error for itemId={}: {}", request.getItemId(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error calling MFCS API for itemId={}", request.getItemId(), ex);
            throw new MfcsApiException("Unexpected error calling MFCS API", ex);
        }
    }
}
