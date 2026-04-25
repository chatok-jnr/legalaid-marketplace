package com.legal_marketplace.legal_marketplace.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class GigMediaResponse {
    private UUID gigId;
    private int serialNo;

    private String url;
    private String publicId;
    private String resourceType;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
