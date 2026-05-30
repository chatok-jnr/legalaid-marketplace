package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.ContractDispute;
import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import com.legal_marketplace.legal_marketplace.repository.projections.ContractDisputeBasicViewProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractDisputeRepository extends JpaRepository<ContractDispute, UUID> {
    Optional<ContractDispute> findByContractId(UUID contractId);
    boolean existsByContractId(UUID contractId);

    @Query(value = """
    SELECT\s
    cd.id AS id,
    cd.contract_id as contractId,
    cd.dispute_status AS disputeStatus,
    cd.dispute_opened_by AS disputeOpenedBy,
    c.client_id AS client_id,
    c.price_at_hire AS price,
    g.title AS title
    FROM contract_disputes cd
    LEFT JOIN contracts c ON c.id = cd.contract_id
    LEFT JOIN gigs g ON c.gig_id = g.id
    WHERE dispute_status = :status
""", nativeQuery = true)
    List<ContractDisputeBasicViewProjection> findAllContractDispute(@Param("status") String status);


    @Query(value = """
    SELECT\s
    cd.id AS id,
    cd.contract_id as contractId,
    cd.dispute_status AS disputeStatus,
    cd.dispute_opened_by AS disputeOpenedBy,
    c.client_id AS client_id,
    c.price_at_hire AS price,
    g.title AS title
    FROM contract_disputes cd
    LEFT JOIN contracts c ON c.id = cd.contract_id
    LEFT JOIN gigs g ON c.gig_id = g.id
    WHERE dispute_status = :status and admin_id = :adminId
""", nativeQuery = true)
    List<ContractDisputeBasicViewProjection> findMyContractDispute(@Param("status") String status, @Param("adminId") UUID adminId);

}
