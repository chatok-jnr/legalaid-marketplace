package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

public class ContractDeliveryRequest {

    @Data
    public static class Create{
        @NotNull
        private UUID contractId;
        @NotNull
        private String deliveryNote;
    }

    @Data
    public static class Revision{
        private String revisionNote;
    }


}
