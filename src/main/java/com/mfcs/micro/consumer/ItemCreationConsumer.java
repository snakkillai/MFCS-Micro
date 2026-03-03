package com.mfcs.micro.consumer;

import com.mfcs.micro.exception.AvroValidationException;
import com.mfcs.micro.exception.MfcsApiException;
import com.mfcs.micro.model.ItemCreationEvent;
import com.mfcs.micro.model.MfcsItemRequest;
import com.mfcs.micro.model.MfcsItemResponse;
import com.mfcs.micro.service.AvroValidationService;
import com.mfcs.micro.service.MfcsApiService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens for Item Creation events on the configured topic.
 * <p>
 * On receiving a message:
 * 1. Validates the Avro payload against the ItemCreationEvent schema.
 * 2. Maps the event to an MFCS API request.
 * 3. Calls the Oracle MFCS REST API (OAuth2-protected) to create the item.
 * 4. Acknowledges the Kafka message on success.
 */
@Component
public class ItemCreationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ItemCreationConsumer.class);

    private final AvroValidationService avroValidationService;
    private final MfcsApiService mfcsApiService;

    public ItemCreationConsumer(AvroValidationService avroValidationService,
                                MfcsApiService mfcsApiService) {
        this.avroValidationService = avroValidationService;
        this.mfcsApiService = mfcsApiService;
    }

    /**
     * Listens to the item creation Kafka topic and processes each message.
     *
     * @param record         the Kafka consumer record containing the Avro payload
     * @param acknowledgment manual acknowledgment to commit the offset after processing
     * @param topic          the Kafka topic the message was received from
     * @param partition      the partition the message was received from
     * @param offset         the offset of the message
     */
    @KafkaListener(
            topics = "${kafka.topics.item-creation}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, ItemCreationEvent> record,
                        Acknowledgment acknowledgment,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received message from topic={}, partition={}, offset={}, key={}",
                topic, partition, offset, record.key());

        ItemCreationEvent event = record.value();

        try {
            // Step 1: Validate the Avro message against schema
            avroValidationService.validate(event);
            log.debug("Avro validation passed for eventId={}", event.getEventId());

            // Step 2: Map Avro event to MFCS API request
            MfcsItemRequest request = mapToMfcsRequest(event);

            // Step 3: Call Oracle MFCS REST API with OAuth2 authentication
            MfcsItemResponse response = mfcsApiService.createItem(request);

            log.info("Item creation successful: eventId={}, itemId={}, mfcsRef={}",
                    event.getEventId(), event.getItemId(),
                    response != null ? response.getMfcsReferenceId() : null);

            // Step 4: Acknowledge the Kafka message offset
            acknowledgment.acknowledge();

        } catch (AvroValidationException ex) {
            log.error("Avro validation failed for message at topic={}, partition={}, offset={}: {}",
                    topic, partition, offset, ex.getMessage());
            // Acknowledge to skip the invalid message and prevent infinite retry
            acknowledgment.acknowledge();

        } catch (MfcsApiException ex) {
            log.error("MFCS API call failed for eventId={}, statusCode={}: {}",
                    event != null ? event.getEventId() : "unknown", ex.getStatusCode(), ex.getMessage());
            // Do not acknowledge - allows Kafka to retry or route to DLQ
            throw ex;

        } catch (Exception ex) {
            log.error("Unexpected error processing message at topic={}, partition={}, offset={}",
                    topic, partition, offset, ex);
            throw ex;
        }
    }

    /**
     * Maps an {@link ItemCreationEvent} Avro record to an {@link MfcsItemRequest}.
     */
    private MfcsItemRequest mapToMfcsRequest(ItemCreationEvent event) {
        return MfcsItemRequest.builder()
                .eventId(event.getEventId())
                .itemId(event.getItemId())
                .itemCode(event.getItemCode())
                .itemName(event.getItemName())
                .description(event.getDescription())
                .category(event.getCategory())
                .unitOfMeasure(event.getUnitOfMeasure())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .createdBy(event.getCreatedBy())
                .createdDate(event.getCreatedDate() != null ? event.getCreatedDate().toEpochMilli() : 0L)
                .sourceSystem(event.getSourceSystem())
                .build();
    }
}
