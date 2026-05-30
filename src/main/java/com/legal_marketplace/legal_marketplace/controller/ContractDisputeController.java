package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDisputeRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDisputeResponse;
import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import com.legal_marketplace.legal_marketplace.service.ContractDisputeService;
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
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class ContractDisputeController {
    /**
     * Get authenticated user email from SecurityContext
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    private final ContractDisputeService contractDisputeService;

    // Create dispute
    @PostMapping("")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT')")
    public ResponseEntity<ContractDisputeResponse.Create> createContractDispute(
            @Valid @RequestBody
            ContractDisputeRequest.Create request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contractDisputeService.create(request, getAuthenticatedUserEmail()));
    }

    // Get all dispute id where no admin is assigned
    @GetMapping("/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContractDisputeResponse.BasicView>> getDisputes(
            @PathVariable("status") DisputeStatus status
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractDisputeService.getDisputes(status,  getAuthenticatedUserEmail()));
    }

    // Get Dispute in Details
    @GetMapping("/details/{disputeId})")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractDisputeResponse.ExtendedView> disputeExtendedView(
            @PathVariable("disputeId") UUID disputeId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractDisputeService.getDisputesExtendedView(disputeId));
    }

    // Assign Admin into a dispute
    @PatchMapping("/{disputeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignAdminToContractDispute(
            @PathVariable("disputeId") UUID disputeId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractDisputeService.assignAdmin(disputeId, getAuthenticatedUserEmail()));
    }

    // Resolved a dispute
    @PatchMapping("/{disputeId}/resolved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resolveDispute(
            @PathVariable("disputeId") UUID disputeId,
            @RequestBody String resolution
    ) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractDisputeService.resolveDispute(disputeId, resolution, getAuthenticatedUserEmail()));
    }

}
