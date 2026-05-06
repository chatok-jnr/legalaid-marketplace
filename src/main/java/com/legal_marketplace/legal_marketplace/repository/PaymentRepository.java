package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.ContractPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<ContractPayment, UUID> {
    Optional<ContractPayment> findPaymentByContractId(UUID contractId);
    boolean existsByContractId(UUID contractId);
}
