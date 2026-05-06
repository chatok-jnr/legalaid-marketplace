package com.legal_marketplace.legal_marketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contract_deliveries")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ContractDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "delivery_number", nullable = false)
    private int deliveryNumber = 1;

    @Column(name = "delivered_by", nullable = false)
    private UUID deliveredBy;

    @Column(name = "delivery_note", columnDefinition = "TEXT")
    private String deliveryNote;

    //======================================================================
    // Revision request for this delivery
    @Column(name = "revision_requested_at")
    private Instant revisionRequestedAt;

    @Column(name = "revision_note", columnDefinition = "TEXT")
    private String revisionNote;
    //=======================================================================

    @Column(name = "delivered_at")
    private Instant deliveredAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;
}
