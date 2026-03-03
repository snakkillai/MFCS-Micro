package com.mfcs.micro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response received from the Oracle MFCS REST API after creating an item.
 */
public class MfcsItemResponse {

    @JsonProperty("itemId")
    private String itemId;

    @JsonProperty("itemCode")
    private String itemCode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("mfcsReferenceId")
    private String mfcsReferenceId;

    public MfcsItemResponse() {
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMfcsReferenceId() {
        return mfcsReferenceId;
    }

    public void setMfcsReferenceId(String mfcsReferenceId) {
        this.mfcsReferenceId = mfcsReferenceId;
    }

    @Override
    public String toString() {
        return "MfcsItemResponse{" +
                "itemId='" + itemId + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", mfcsReferenceId='" + mfcsReferenceId + '\'' +
                '}';
    }
}
