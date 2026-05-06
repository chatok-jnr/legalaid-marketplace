package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.ContractDispute;
import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractDisputeRepository extends JpaRepository<ContractDispute, UUID> {
    Optional<ContractDispute> findByContractId(UUID contractId);
    boolean existsByContractId(UUID contractId);
    List<ContractDispute> findByDisputeStatus(DisputeStatus status);
}
