package com.legal_marketplace.legal_marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class GigMediaRequest {
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Create{
        private UUID gigId;
        private int serialNo;
        private String url;
        private String publicId;
        private String resourceType;
    }

    @Builder
    @Data
    public static class Update{
        private int newSerialNo;
    }
}
