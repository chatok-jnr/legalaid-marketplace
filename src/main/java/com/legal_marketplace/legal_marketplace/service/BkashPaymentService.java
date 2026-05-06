package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.BkashPaymentRequest;
import com.legal_marketplace.legal_marketplace.dto.response.BkashPaymentResponse;

import java.util.UUID;

public interface BkashPaymentService {
    BkashPaymentResponse.Create create(BkashPaymentRequest.Create request, String userEmail);

    BkashPaymentResponse.Details getByContractPaymentId(UUID contractPaymentId, String userEmail);

    BkashPaymentResponse.Verification verify(UUID contractPaymentId, String userEmail);

    BkashPaymentResponse.Verification reject(UUID contractPaymentId, BkashPaymentRequest.Reject request, String userEmail);
}
