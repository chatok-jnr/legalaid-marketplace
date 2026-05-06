package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.ContractDeliveryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractDeliveryFileRepository extends JpaRepository<ContractDeliveryFile, UUID> {
    List<ContractDeliveryFile> findByDelivery_Id(UUID deliveryId);
    Optional<ContractDeliveryFile> findByIdAndDelivery_Id(UUID id, UUID deliveryId);
}
