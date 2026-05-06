package com.legal_marketplace.legal_marketplace.entity;

import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contract_disputes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDispute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "dispute_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DisputeStatus disputeStatus = DisputeStatus.OPEN;

    @Column(name = "dispute_reason", columnDefinition = "TEXT", nullable = false)
    private String disputeReason;

    @Column(name = "dispute_opened_by", nullable = false)
    private UUID disputeOpenedBy;

    @Column(name = "admin_id")
    private UUID adminId;

    @Column(name = "dispute_opened_at", nullable = false)
    private Instant disputeOpenedAt = Instant.now();

    @Column(name = "dispute_resolved_at")
    private Instant disputeResolvedAt;

    @Column(name = "dispute_resolution", columnDefinition = "TEXT")
    private String disputeResolution;
}
