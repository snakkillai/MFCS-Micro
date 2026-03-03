package com.mfcs.micro.service;

import com.mfcs.micro.exception.AvroValidationException;
import com.mfcs.micro.model.ItemCreationEvent;
import com.mfcs.micro.model.ItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvroValidationServiceTest {

    private AvroValidationService avroValidationService;

    @BeforeEach
    void setUp() {
        avroValidationService = new AvroValidationService();
    }

    @Test
    void validate_validEvent_noException() {
        ItemCreationEvent event = buildValidEvent();
        assertThatCode(() -> avroValidationService.validate(event)).doesNotThrowAnyException();
    }

    @Test
    void validate_nullEvent_throwsException() {
        assertThatThrownBy(() -> avroValidationService.validate(null))
                .isInstanceOf(AvroValidationException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void validate_missingItemId_throwsException() {
        ItemCreationEvent event = buildValidEvent();
        event.setItemId("");
        assertThatThrownBy(() -> avroValidationService.validate(event))
                .isInstanceOf(AvroValidationException.class)
                .hasMessageContaining("itemId");
    }

    @Test
    void validate_missingItemCode_throwsException() {
        ItemCreationEvent event = buildValidEvent();
        event.setItemCode("");
        assertThatThrownBy(() -> avroValidationService.validate(event))
                .isInstanceOf(AvroValidationException.class)
                .hasMessageContaining("itemCode");
    }

    @Test
    void validate_missingItemName_throwsException() {
        ItemCreationEvent event = buildValidEvent();
        event.setItemName(null);
        assertThatThrownBy(() -> avroValidationService.validate(event))
                .isInstanceOf(AvroValidationException.class)
                .hasMessageContaining("itemName");
    }

    @Test
    void validate_nullStatus_throwsException() {
        ItemCreationEvent event = buildValidEvent();
        event.setStatus(null);
        assertThatThrownBy(() -> avroValidationService.validate(event))
                .isInstanceOf(AvroValidationException.class)
                .hasMessageContaining("status");
    }

    @Test
    void validate_zeroCreatedDate_throwsException() {
        ItemCreationEvent event = buildValidEvent();
        event.setCreatedDate(Instant.EPOCH);
        assertThatThrownBy(() -> avroValidationService.validate(event))
                .isInstanceOf(AvroValidationException.class)
                .hasMessageContaining("createdDate");
    }

    @Test
    void validate_missingCategory_throwsException() {
        ItemCreationEvent event = buildValidEvent();
        event.setCategory(null);
        assertThatThrownBy(() -> avroValidationService.validate(event))
                .isInstanceOf(AvroValidationException.class)
                .hasMessageContaining("category");
    }

    private ItemCreationEvent buildValidEvent() {
        return ItemCreationEvent.newBuilder()
                .setEventId("evt-001")
                .setItemId("item-123")
                .setItemCode("CODE-001")
                .setItemName("Test Item")
                .setDescription("A test item")
                .setCategory("ELECTRONICS")
                .setUnitOfMeasure("EACH")
                .setStatus(ItemStatus.ACTIVE)
                .setCreatedBy("user1")
                .setCreatedDate(Instant.now())
                .setSourceSystem("ERP")
                .build();
    }
}
