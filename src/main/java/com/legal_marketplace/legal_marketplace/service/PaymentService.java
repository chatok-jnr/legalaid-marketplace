package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.PaymentRequest;
import com.legal_marketplace.legal_marketplace.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse.Create createPayment(PaymentRequest.Create request, String userEmail);
}
