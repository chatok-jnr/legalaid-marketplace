package com.legal_marketplace.legal_marketplace.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Builder
@Data
public class GigReviewId implements Serializable {
    private UUID gigId;
    private UUID userId;
}
