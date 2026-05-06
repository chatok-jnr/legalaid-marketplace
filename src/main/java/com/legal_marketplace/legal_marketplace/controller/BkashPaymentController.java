package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.BkashPaymentRequest;
import com.legal_marketplace.legal_marketplace.dto.response.BkashPaymentResponse;
import com.legal_marketplace.legal_marketplace.service.BkashPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/bkash-payments")
@RequiredArgsConstructor
public class BkashPaymentController {
    private final BkashPaymentService bkashPaymentService;

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BkashPaymentResponse.Create> create(
            @Valid @RequestBody BkashPaymentRequest.Create request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bkashPaymentService.create(request, getAuthenticatedUserEmail()));
    }

    @GetMapping("/contract-payment/{contractPaymentId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'LAWYER', 'ADMIN')")
    public ResponseEntity<BkashPaymentResponse.Details> getByContractPaymentId(
            @PathVariable UUID contractPaymentId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bkashPaymentService.getByContractPaymentId(contractPaymentId, getAuthenticatedUserEmail()));
    }

    @PatchMapping("/contract-payment/{contractPaymentId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BkashPaymentResponse.Verification> verify(
            @PathVariable UUID contractPaymentId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bkashPaymentService.verify(contractPaymentId, getAuthenticatedUserEmail()));
    }

    @PatchMapping("/contract-payment/{contractPaymentId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BkashPaymentResponse.Verification> reject(
            @PathVariable UUID contractPaymentId,
            @Valid @RequestBody BkashPaymentRequest.Reject request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bkashPaymentService.reject(contractPaymentId, request, getAuthenticatedUserEmail()));
    }
}
