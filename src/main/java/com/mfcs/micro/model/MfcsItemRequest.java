package com.mfcs.micro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload sent to the Oracle MFCS REST API when creating an item.
 */
public class MfcsItemRequest {

    @JsonProperty("itemId")
    private String itemId;

    @JsonProperty("itemCode")
    private String itemCode;

    @JsonProperty("itemName")
    private String itemName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("unitOfMeasure")
    private String unitOfMeasure;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("createdDate")
    private long createdDate;

    @JsonProperty("sourceSystem")
    private String sourceSystem;

    @JsonProperty("eventId")
    private String eventId;

    public MfcsItemRequest() {
    }

    private MfcsItemRequest(Builder builder) {
        this.itemId = builder.itemId;
        this.itemCode = builder.itemCode;
        this.itemName = builder.itemName;
        this.description = builder.description;
        this.category = builder.category;
        this.unitOfMeasure = builder.unitOfMeasure;
        this.status = builder.status;
        this.createdBy = builder.createdBy;
        this.createdDate = builder.createdDate;
        this.sourceSystem = builder.sourceSystem;
        this.eventId = builder.eventId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getEventId() {
        return eventId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String itemId;
        private String itemCode;
        private String itemName;
        private String description;
        private String category;
        private String unitOfMeasure;
        private String status;
        private String createdBy;
        private long createdDate;
        private String sourceSystem;
        private String eventId;

        public Builder itemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public Builder itemCode(String itemCode) {
            this.itemCode = itemCode;
            return this;
        }

        public Builder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder unitOfMeasure(String unitOfMeasure) {
            this.unitOfMeasure = unitOfMeasure;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdDate(long createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder sourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public MfcsItemRequest build() {
            return new MfcsItemRequest(this);
        }
    }
}
