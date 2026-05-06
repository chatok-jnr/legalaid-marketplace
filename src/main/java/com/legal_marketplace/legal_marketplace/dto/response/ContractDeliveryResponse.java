package com.legal_marketplace.legal_marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ContractDeliveryResponse {

    @Builder
    @Data
    public static class Create {
        private UUID id;
        private UUID contractId;

        private int deliveryNumber;
        private UUID deliveredBy;
        private String deliveryNote;

        private Instant revisionRequestedAt;
        private String revisionNote;

        private Instant deliveredAt;
        private Instant completedAt;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryFile {
        private UUID id;
        private String fileName;
        private String url;
        private int fileSize;
        private String mimeType;
    }

    @Builder
    @Data
    public static class AllInfo {
        private UUID deliveryId;
        private UUID clientId;
        private UUID lawyerId;
        private UUID contractId;

        private String deliveryNote;
        private Instant deliveredAt;
        private Instant revisionRequestedAt;
        private String revisionNote;
        private Instant completedAt;

        // All files associated with this delivery
        private List<DeliveryFile> deliveryFiles;
    }

    @Builder
    @Data
    public static class Revision {
        private Instant revisionRequestedAt;
        private String revisionNote;
    }
}
