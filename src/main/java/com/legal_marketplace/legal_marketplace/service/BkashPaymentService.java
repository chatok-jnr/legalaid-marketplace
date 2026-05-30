package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.BkashPaymentRequest;
import com.legal_marketplace.legal_marketplace.dto.response.BkashPaymentResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface BkashPaymentService {
    BkashPaymentResponse.Create create(BkashPaymentRequest.Create request, String userEmail);

    BkashPaymentResponse.Details getByContractPaymentId(UUID contractPaymentId, String userEmail);

    BkashPaymentResponse.Verification verify(UUID contractPaymentId, String userEmail);

    BkashPaymentResponse.Verification reject(UUID contractPaymentId, BkashPaymentRequest.Reject request, String userEmail);

    // For admin
    List<BkashPaymentResponse.BasicView> getAllByStatusForAdmin(String status, int page, int size, String adminEmail);
    BkashPaymentResponse.ExtendedView getPaymentDetailsById(UUID id);
    BkashPaymentResponse.ExtendedView updStatus(UUID id, BkashPaymentRequest.UpdateStatus request, String adminEmail);
}
