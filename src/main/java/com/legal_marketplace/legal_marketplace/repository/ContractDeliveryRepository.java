package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.ContractDelivery;
import com.legal_marketplace.legal_marketplace.repository.projectiions.ContractDeliveryWithFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractDeliveryRepository extends JpaRepository<ContractDelivery, UUID> {
    List<ContractDelivery> findByContractId(UUID contractId);
    ContractDelivery findByContractIdAndDeliveryNumber(UUID contractId, int deliveryNumber);
    int countByContractId(UUID contractId);

    @Query(value = """
            SELECT
                cd.id AS deliveryId,
                c.client_id AS clientId,
                c.lawyer_id AS lawyerId,
                c.id AS contractId,
                cd.delivery_note AS deliveryNote,
                cd.delivered_at AS deliveredAt,
                cd.revision_requested_at AS revisionRequestedAt,
                cd.revision_note AS revisionNote,
                cd.completed_at AS completedAt,
                COALESCE(
                    (
                        SELECT jsonb_agg(
                            jsonb_build_object(
                                'id', cdf.id,
                                'fileName', cdf.file_name,
                                'url', cdf.file_url,
                                'fileSize', cdf.file_size,
                                'mimeType', cdf.mime_type
                            )
                            ORDER BY cdf.created_at, cdf.id
                        )
                        FROM contract_delivery_files cdf
                        WHERE cdf.delivery_id = cd.id
                    ),
                    '[]'::jsonb
                )::text AS deliveryFiles
            FROM contract_deliveries cd
            LEFT JOIN contracts c ON cd.contract_id = c.id
            WHERE cd.id = :deliveryId
            """, nativeQuery = true)
    ContractDeliveryWithFiles findWithFilesByDeliveryId(UUID deliveryId);
}
