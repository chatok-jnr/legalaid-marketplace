package com.legal_marketplace.legal_marketplace.dto.response;

import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

public class ContractDisputeResponse {
    @Data
    @Builder
    public static class Create{
        private UUID disputeId;
        private UUID contractId;
        private String disputeReason;
        private DisputeStatus disputeStatus;
        private UUID disputeOpenedBy;
    }

    @Data
    @Builder
    public static class AllInfo{
        private UUID disputeId;
        private UUID contractId;

        private DisputeStatus disputeStatus;
        private String disputeReason;
        private UUID disputeOpenedBy;
        private UUID adminId;
        
        private Instant disputeOpenedAt;
        private Instant disputeResolvedAt;
        private String disputeResolution;
    }
}
