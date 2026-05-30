package com.legal_marketplace.legal_marketplace.dto.request;

import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

public class ContractDisputeRequest {
    @Data
    public static class Create{
        @NotNull
        private UUID contractId;
        @NotNull
        private String disputeReason;
    }

    @Data
    public static class Update{
        private DisputeStatus disputeStatus;
        private UUID adminId;
        private String disputeResolution;
    }

    @Data
    @Builder
    public static class Resolution{
        @NotNull
        private String disputeResolution;
    }
}
