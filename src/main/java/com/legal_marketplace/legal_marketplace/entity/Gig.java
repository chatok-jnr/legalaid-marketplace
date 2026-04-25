package com.legal_marketplace.legal_marketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jdk.jfr.Timespan;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "gigs")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Gig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "lawyer_id", nullable = false, updatable = false)
    private UUID lawyerId;

    @Column(name = "title", nullable = false)
    @Length(max = 100)
    private String title;

    @Column(name = "min_price", nullable = false)
    @Min(1)
    private int minPrice;

    @Column(name = "about_this_gig", columnDefinition = "TEXT")
    private String aboutThisGig;

    @Column(name = "is_public")
    private boolean isPublic = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
