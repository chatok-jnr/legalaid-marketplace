package com.legal_marketplace.legal_marketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gig_reviews")
@Builder
@Data
public class GigReview {
    @EmbeddedId
    private GigReviewId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gigId", insertable = false, updatable = false)
    private Gig gig;

    @Column(name = "rating", nullable = false)
    @Min(1)
    @Max(5)
    private int rating;

    @Column(name = "comment")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime created_at;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updated_at;
}
