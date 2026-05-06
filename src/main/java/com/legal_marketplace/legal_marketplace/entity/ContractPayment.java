package com.legal_marketplace.legal_marketplace.entity;

import com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contract_payments")
public class ContractPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Contract contract;

    @Column(name = "platform_fee_amount", nullable = false)
    private int platformFeeAmount;

    @Column(name = "platform_fee_percent", precision = 5, scale = 2, nullable = false, columnDefinition = "numeric(5,2) default 10.00")
    private BigDecimal platformFeePercent = new BigDecimal("10.00");

    @Column(name = "lawyer_payout_amount", nullable = false)
    private int lawyerPayoutAmount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "payment_status", nullable = false, columnDefinition = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "escrow_released_at")
    private OffsetDateTime escrowReleasedAt;
}
