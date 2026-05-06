package com.legal_marketplace.legal_marketplace.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

public class ContractDeliveryFileResponse {
    @Data
    public static class Create {
        private UUID id;
        private UUID deliveryId;
        private String fileName;
        private String fileUrl;
        private int fileSize;
        private String mimeType;
        private UUID uploadedBy;
        private Instant createdAt;
    }
}
