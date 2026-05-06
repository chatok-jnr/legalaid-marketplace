package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
        @NotNull
        private UUID gigId;
        @NotNull
        @Min(1)
        private Integer serialNo;
        @NotBlank
        private String url;
        @NotBlank
        private String publicId;
        @NotBlank
        @Pattern(regexp = "^(image|video|raw)$", message = "resourceType must be image, video, or raw")
        private String resourceType;
    }

    @Builder
    @Data
    public static class Update{
        @NotNull
        @Min(1)
        private Integer newSerialNo;
    }
}
