package com.mfcs.micro.service;

import com.mfcs.micro.exception.MfcsApiException;
import com.mfcs.micro.model.MfcsItemRequest;
import com.mfcs.micro.model.MfcsItemResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MfcsApiServiceTest {

    private MockWebServer mockWebServer;
    private MfcsApiService mfcsApiService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/api/v1").toString();
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        mfcsApiService = new MfcsApiService(webClient, "/items", 5);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void createItem_success_returnsResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"itemId\":\"item-123\",\"itemCode\":\"CODE-001\","
                        + "\"status\":\"CREATED\",\"message\":\"Item created\","
                        + "\"mfcsReferenceId\":\"MFCS-REF-001\"}"));

        MfcsItemRequest request = buildSampleRequest();
        MfcsItemResponse response = mfcsApiService.createItem(request);

        assertThat(response).isNotNull();
        assertThat(response.getItemId()).isEqualTo("item-123");
        assertThat(response.getMfcsReferenceId()).isEqualTo("MFCS-REF-001");
        assertThat(response.getStatus()).isEqualTo("CREATED");
    }

    @Test
    void createItem_400Response_throwsMfcsApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\":\"Invalid item code\"}"));

        MfcsItemRequest request = buildSampleRequest();

        assertThatThrownBy(() -> mfcsApiService.createItem(request))
                .isInstanceOf(MfcsApiException.class)
                .hasMessageContaining("400");
    }

    @Test
    void createItem_500Response_throwsMfcsApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\":\"Internal server error\"}"));

        MfcsItemRequest request = buildSampleRequest();

        assertThatThrownBy(() -> mfcsApiService.createItem(request))
                .isInstanceOf(MfcsApiException.class);
    }

    @Test
    void createItem_401Response_throwsMfcsApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\":\"Unauthorized\"}"));

        MfcsItemRequest request = buildSampleRequest();

        assertThatThrownBy(() -> mfcsApiService.createItem(request))
                .isInstanceOf(MfcsApiException.class)
                .hasMessageContaining("401");
    }

    private MfcsItemRequest buildSampleRequest() {
        return MfcsItemRequest.builder()
                .eventId("evt-001")
                .itemId("item-123")
                .itemCode("CODE-001")
                .itemName("Test Item")
                .description("A test item")
                .category("ELECTRONICS")
                .unitOfMeasure("EACH")
                .status("ACTIVE")
                .createdBy("user1")
                .createdDate(System.currentTimeMillis())
                .sourceSystem("ERP")
                .build();
    }
}
