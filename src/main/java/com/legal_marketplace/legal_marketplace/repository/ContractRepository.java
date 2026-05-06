package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.dto.response.ContractResponse;
import com.legal_marketplace.legal_marketplace.entity.Contract;
import com.legal_marketplace.legal_marketplace.repository.projectiions.ContractExtendedViewProjection;
import com.legal_marketplace.legal_marketplace.repository.projectiions.ContractViewProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
    @Query(value = """
    SELECT\s
    	cl.full_name AS clientName,
    	lw.full_name AS lawyerName,
    	g.title AS gigTitle,
    	co.id as id,
    	co.price_at_hire AS priceAtHire,
    	co.status AS status,
    	co.created_at AS createdAt
    FROM contracts co
    LEFT JOIN users cl ON co.client_id = cl.id
    LEFT JOIN users lw ON co.lawyer_id = lw.id\s
    LEFT JOIN gigs g ON co.gig_id = g.id
    WHERE cl.id = :userId;
""", nativeQuery = true)
    List<ContractViewProjection> findClientContractsByUserId(UUID userId);

    @Query(value = """
    SELECT\s
    	cl.full_name AS clientName,
    	lw.full_name AS lawyerName,
    	g.title AS gigTitle,
    	co.id as id,
    	co.price_at_hire AS priceAtHire,
    	co.status AS status,
    	co.created_at AS createdAt
    FROM contracts co
    LEFT JOIN users cl ON co.client_id = cl.id
    LEFT JOIN users lw ON co.lawyer_id = lw.id\s
    LEFT JOIN gigs g ON co.gig_id = g.id
    WHERE lw.id = :userId;
""", nativeQuery = true)
    List<ContractViewProjection> findLawyerContractsByUserId(UUID userId);

    @Query(value = """
    SELECT\s
    	cln.full_name 			AS clientName,
    	lwr.full_name 			AS lawyerName,
    	g.title 				AS gigTitle,
    	g.id 					AS gigId,
    	ct.id                   AS id,
    	ct.client_id 			AS clientId,
    	ct.lawyer_id 			AS lawyerId,
    	ct.price_at_hire 		AS priceAtHire,
    	ct.requirements 		AS requirements,
    	ct.revisions_left 		AS revisionLeft,
    	ct.status 				AS status,
    	ct.cancellation_reason 	AS cancellationReason,
    	ct.cancelled_at 		AS cancelledAt,
    	ct.cancelled_by 		AS cancelledBy,
    	ct.delivery_deadline 	AS deliveryDeadline,
    	ct.created_at 			AS createdAt,
    	ct.updated_at 			AS updatedAt,
    	cp.id 					AS paymentId,
    	cp.platform_fee_amount	AS platformFeeAmount,
    	cp.lawyer_payout_amount	AS lawyerPayoutAmount,
    	cp.payment_status 		AS paymentStatus,
    	cp.payment_reference	AS paymentReference,
    	cp.paid_at				AS paidAt,\s
    	cp.escrow_released_at	AS escrowReleasedAt,
    	cd.id 					AS disputeId,
    	cd.dispute_status		AS disputeStatus,
    	cd.dispute_reason		AS disputeReason,
    	cd.dispute_opened_by 	AS disputeOpenedBy,
    	cd.admin_id 			AS disputeAdminId,
    	cd.dispute_opened_at 	AS disputeOpenedAt,
    	cd.dispute_resolved_at	AS disputeResolvedAt,
    	cd.dispute_resolution	AS disputeResolution
    FROM contracts  ct\s
    LEFT JOIN users cln ON ct.client_id = cln.id\s
    LEFT JOIN users lwr ON ct.lawyer_id = lwr.id\s
    LEFT JOIN gigs 	g 	ON ct.gig_id = g.id
    LEFT JOIN contract_payments cp ON ct.id = cp.contract_id
    LEFT JOIN contract_disputes cd ON ct.id = cd.contract_id
    WHERE ct.id = :contractId;
""", nativeQuery = true)
    ContractExtendedViewProjection findContractById(UUID contractId);
}
