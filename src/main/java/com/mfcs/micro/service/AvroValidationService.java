package com.mfcs.micro.service;

import com.mfcs.micro.exception.AvroValidationException;
import com.mfcs.micro.model.ItemCreationEvent;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service that validates an incoming Avro message against the ItemCreationEvent schema.
 * Ensures all required fields are present and non-null before processing.
 */
@Service
public class AvroValidationService {

    private static final Logger log = LoggerFactory.getLogger(AvroValidationService.class);

    /**
     * Validates the incoming Avro record against required fields defined in the schema.
     *
     * @param record the Avro record to validate
     * @throws AvroValidationException if required fields are missing or null
     */
    public void validate(ItemCreationEvent record) {
        if (record == null) {
            throw new AvroValidationException("ItemCreationEvent record must not be null");
        }

        validateRequiredString(record.getEventId(), "eventId");
        validateRequiredString(record.getItemId(), "itemId");
        validateRequiredString(record.getItemCode(), "itemCode");
        validateRequiredString(record.getItemName(), "itemName");
        validateRequiredString(record.getCategory(), "category");
        validateRequiredString(record.getUnitOfMeasure(), "unitOfMeasure");

        if (record.getStatus() == null) {
            throw new AvroValidationException("Required field 'status' is null");
        }
        if (record.getCreatedDate() == null || record.getCreatedDate().toEpochMilli() <= 0) {
            throw new AvroValidationException("Required field 'createdDate' must be a positive timestamp");
        }
        validateRequiredString(record.getCreatedBy(), "createdBy");

        log.debug("Avro validation passed for eventId={}, itemId={}", record.getEventId(), record.getItemId());
    }

    /**
     * Validates that a string field in an Avro GenericRecord is present and non-empty.
     *
     * @param record    the generic Avro record
     * @param fieldName the field to validate
     * @throws AvroValidationException if the field is missing or blank
     */
    public void validateField(GenericRecord record, String fieldName) {
        if (record == null) {
            throw new AvroValidationException("Avro record must not be null");
        }
        Schema.Field field = record.getSchema().getField(fieldName);
        if (field == null) {
            throw new AvroValidationException("Field '" + fieldName + "' not found in schema");
        }
        Object value = record.get(fieldName);
        if (value == null || value.toString().isBlank()) {
            throw new AvroValidationException("Required field '" + fieldName + "' is missing or blank");
        }
    }

    private void validateRequiredString(Object value, String fieldName) {
        if (value == null || value.toString().isBlank()) {
            throw new AvroValidationException("Required field '" + fieldName + "' is missing or blank");
        }
    }
}
