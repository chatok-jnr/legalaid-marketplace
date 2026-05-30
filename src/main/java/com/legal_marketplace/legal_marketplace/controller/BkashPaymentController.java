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
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // For Admin
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BkashPaymentResponse.BasicView>> getBkashPayments(
            @RequestParam String status,
            @RequestParam int page
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bkashPaymentService.getAllByStatusForAdmin(status, page, 20, getAuthenticatedUserEmail()));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BkashPaymentResponse.ExtendedView> getBkashPaymentById(
            @PathVariable UUID id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bkashPaymentService.getPaymentDetailsById(id));
    }

    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BkashPaymentResponse.ExtendedView> updateBkashPaymentById(
            @PathVariable("id") UUID id,
            @RequestBody BkashPaymentRequest.UpdateStatus request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bkashPaymentService.updStatus(id, request, getAuthenticatedUserEmail()));
    }
}
