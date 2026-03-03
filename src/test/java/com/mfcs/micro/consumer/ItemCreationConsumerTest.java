package com.mfcs.micro.consumer;

import com.mfcs.micro.exception.AvroValidationException;
import com.mfcs.micro.exception.MfcsApiException;
import com.mfcs.micro.model.ItemCreationEvent;
import com.mfcs.micro.model.ItemStatus;
import com.mfcs.micro.model.MfcsItemResponse;
import com.mfcs.micro.service.AvroValidationService;
import com.mfcs.micro.service.MfcsApiService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCreationConsumerTest {

    @Mock
    private AvroValidationService avroValidationService;

    @Mock
    private MfcsApiService mfcsApiService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private ItemCreationConsumer consumer;

    private ItemCreationEvent validEvent;
    private ConsumerRecord<String, ItemCreationEvent> consumerRecord;

    @BeforeEach
    void setUp() {
        validEvent = ItemCreationEvent.newBuilder()
                .setEventId("evt-001")
                .setItemId("item-123")
                .setItemCode("CODE-001")
                .setItemName("Test Item")
                .setDescription("A test item description")
                .setCategory("ELECTRONICS")
                .setUnitOfMeasure("EACH")
                .setStatus(ItemStatus.ACTIVE)
                .setCreatedBy("user1")
                .setCreatedDate(Instant.now())
                .setSourceSystem("ERP")
                .build();

        consumerRecord = new ConsumerRecord<>("item-creation-topic", 0, 0L, "item-123", validEvent);
    }

    @Test
    void consume_validMessage_callsMfcsApiAndAcknowledges() {
        MfcsItemResponse response = new MfcsItemResponse();
        response.setItemId("item-123");
        response.setMfcsReferenceId("MFCS-REF-001");
        when(mfcsApiService.createItem(any())).thenReturn(response);

        consumer.consume(consumerRecord, acknowledgment, "item-creation-topic", 0, 0L);

        verify(avroValidationService).validate(validEvent);
        verify(mfcsApiService).createItem(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void consume_avroValidationFails_acknowledgesAndSkipsApiCall() {
        doThrow(new AvroValidationException("Missing required field 'itemCode'"))
                .when(avroValidationService).validate(any());

        consumer.consume(consumerRecord, acknowledgment, "item-creation-topic", 0, 0L);

        verify(avroValidationService).validate(validEvent);
        verifyNoInteractions(mfcsApiService);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void consume_mfcsApiThrows_doesNotAcknowledge() {
        doThrow(new MfcsApiException("MFCS API error", 500))
                .when(mfcsApiService).createItem(any());

        assertThatThrownBy(() ->
                consumer.consume(consumerRecord, acknowledgment, "item-creation-topic", 0, 0L))
                .isInstanceOf(MfcsApiException.class);

        verify(avroValidationService).validate(validEvent);
        verify(mfcsApiService).createItem(any());
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void consume_mapsEventFieldsCorrectly() {
        MfcsItemResponse response = new MfcsItemResponse();
        response.setItemId("item-123");
        when(mfcsApiService.createItem(any())).thenReturn(response);

        consumer.consume(consumerRecord, acknowledgment, "item-creation-topic", 0, 0L);

        ArgumentCaptor<com.mfcs.micro.model.MfcsItemRequest> requestCaptor =
                ArgumentCaptor.forClass(com.mfcs.micro.model.MfcsItemRequest.class);
        verify(mfcsApiService).createItem(requestCaptor.capture());

        com.mfcs.micro.model.MfcsItemRequest captured = requestCaptor.getValue();
        assertThat(captured.getEventId()).isEqualTo("evt-001");
        assertThat(captured.getItemId()).isEqualTo("item-123");
        assertThat(captured.getItemCode()).isEqualTo("CODE-001");
        assertThat(captured.getItemName()).isEqualTo("Test Item");
        assertThat(captured.getCategory()).isEqualTo("ELECTRONICS");
        assertThat(captured.getUnitOfMeasure()).isEqualTo("EACH");
        assertThat(captured.getStatus()).isEqualTo("ACTIVE");
        assertThat(captured.getCreatedBy()).isEqualTo("user1");
        assertThat(captured.getSourceSystem()).isEqualTo("ERP");
    }
}
