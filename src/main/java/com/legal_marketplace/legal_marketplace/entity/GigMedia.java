package com.legal_marketplace.legal_marketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "gig_media")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GigMedia {
    @EmbeddedId
    private GigMediaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gigId", insertable = false, updatable = false)
    private Gig gig;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
