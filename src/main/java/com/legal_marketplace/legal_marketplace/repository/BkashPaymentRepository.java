package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.BkashPayment;
import com.legal_marketplace.legal_marketplace.entity.enums.BkashPaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BkashPaymentRepository extends JpaRepository<BkashPayment, UUID> {
    Optional<BkashPayment> findByContractPaymentId(UUID contractPaymentId);

    boolean existsByContractPaymentId(UUID contractPaymentId);

    Optional<BkashPayment> findByTransactionId(String transactionId);

    List<BkashPayment> findByStatus(BkashPaymentStatus status);

    // For Admin
    //Page<BkashPayment> findByStatus(BkashPaymentStatus status);
    Page<BkashPayment> findByStatusAndVerifiedBy(BkashPaymentStatus status, UUID adminId, Pageable pageable);
    Page<BkashPayment> findByStatusAndVerifiedByIsNull(BkashPaymentStatus status, Pageable pageable);
}
