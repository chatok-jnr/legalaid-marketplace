package com.legal_marketplace.legal_marketplace.entity;

import com.legal_marketplace.legal_marketplace.entity.enums.BkashPaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bkash_payments")
public class BkashPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_payment_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull(message = "Contract payment is required")
    private ContractPayment contractPayment;

    @NotBlank(message = "Sender number is required")
    @Size(max = 15, message = "Sender number must be at most 15 characters")
    @Pattern(regexp = "^(?:\\+?88)?01[3-9]\\d{8}$", message = "Sender number must be a valid Bangladeshi mobile number")
    @Column(name = "sender_number", length = 15, nullable = false)
    private String senderNumber;

    @NotBlank(message = "Receiver number is required")
    @Size(max = 15, message = "Receiver number must be at most 15 characters")
    @Pattern(regexp = "^(?:\\+?88)?01[3-9]\\d{8}$", message = "Receiver number must be a valid Bangladeshi mobile number")
    @Column(name = "receiver_number", length = 15, nullable = false)
    private String receiverNumber;

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 30, message = "Transaction ID must be at most 30 characters")
    @Column(name = "transaction_id", length = 30, nullable = false)
    private String transactionId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must fit NUMERIC(12,2)")
    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "bkash_payment_status")
    private BkashPaymentStatus status = BkashPaymentStatus.PENDING;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Size(max = 5000, message = "Rejection reason must be at most 5000 characters")
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = BkashPaymentStatus.PENDING;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
