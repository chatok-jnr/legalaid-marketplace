package com.legal_marketplace.legal_marketplace.dto.response;

import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ContractDisputeResponse {
    @Data
    @Builder
    public static class Create{
        private UUID disputeId;
        private UUID contractId;
        private String disputeReason;
        private DisputeStatus disputeStatus;
        private UUID disputeOpenedBy;
    }

    @Data
    @Builder
    public static class AllInfo{
        private UUID disputeId;
        private UUID contractId;

        private DisputeStatus disputeStatus;
        private String disputeReason;
        private UUID disputeOpenedBy;
        private UUID adminId;
        
        private Instant disputeOpenedAt;
        private Instant disputeResolvedAt;
        private String disputeResolution;
    }

    @Data
    @Builder
    public static class BasicView{
        private UUID disputeId;
        private UUID contractId;
        private DisputeStatus disputeStatus;
        private String gigTitle;
        private Integer price;
        private String openedBy;
    }

    @Data
    @Builder
    public static class DeliveryInfo{
        private UUID id;
        private Integer contractDeliveryNumber;
        private String deliveryNote;
        private Instant revisionRequestedAt;
        private String revisionNote;
        private Instant deliveredAt;
    }

    @Data
    @Builder
    public static class ExtendedView{
        private DisputeStatus disputeStatus;
        private String disputeReason;
        private UUID disputeOpenedBy;
        private UUID adminId;
        private Instant disputeOpenedAt;

        private UUID clientId;
        private UUID lawyerId;
        private UUID gigId;
        private String requirements;
        private Integer maxRevision;
        private Integer revisionsLeft;
        private Instant deliveryDeadLine;

        List<DeliveryInfo> deliveryInfos;

        private Instant disputeResolvedAt;
        private String disputeResolution;
    }
}

////COALESCE(
//        (
//        SELECT jsonb_agg(
//                jsonb_build_object(
//                'lcId', lc.id,
//                	'title', lc.title,
//                    'credentialType', lc.credential_type,
//                    'issuingBody', lc.issuing_body,
//                    'url', lc.document_url,
//                    'issuedDate', lc.issued_date,
//					'expiryDate', lc.expiry_date
//        )
//            )
//FROM lawyer_credentials lc
//WHERE lc.lawyer_id = lp.id
//        ),
//                '[]'::jsonb
//    ) AS documents
