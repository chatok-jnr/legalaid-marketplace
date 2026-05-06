package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ContractDeliveryFileRequest {
    @Data
    public static class Create {
        @NotBlank
        private String fileName;

        @NotBlank
        private String fileUrl;

        @Min(1)
        private int fileSize;

        @NotBlank
        private String mimeType;
    }
}
