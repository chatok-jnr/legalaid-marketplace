package com.legal_marketplace.legal_marketplace.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class GigReviewResponse {
    private UUID gigId;
    private UUID userId;
    private int rating;
    private String comment;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
