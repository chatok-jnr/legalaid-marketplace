package com.legal_marketplace.legal_marketplace.entity;

import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "client_id", nullable = false, updatable = false)
    private UUID clientId;

    @Column(name = "lawyer_id", nullable = false, updatable = false)
    private UUID lawyerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gig_id", nullable = false)
    private Gig gig;

    @Column(name = "price_at_hire", nullable = false)
    private int priceAtHire;

    @Column(name = "requirements", columnDefinition = "TEXT", nullable = false)
    private String requirements;

    @Column(name = "max_revision")
    private int maxRevision;

    @Column(name = "revisions_left", nullable = false)
    private int revisionsLeft = 0;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.PENDING;


    // ==================================================================
    // Cancellation (contract-level state, belongs here not in deliveries)
    // ==================================================================
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;


    @Column(name = "delivery_deadline", nullable = false)
    private Instant deliveryDeadline;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt  = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
