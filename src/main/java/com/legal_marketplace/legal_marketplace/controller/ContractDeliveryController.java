package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDeliveryRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryResponse;
import com.legal_marketplace.legal_marketplace.service.ContractDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/contract-deliveries")
@RequiredArgsConstructor
public class ContractDeliveryController {
    /**
     * Get authenticated user email from SecurityContext
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    private final ContractDeliveryService contractDeliveryService;

    // Create Delivery
    @PostMapping("")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<ContractDeliveryResponse.Create> createContractDelivery(
            @Valid
            @RequestBody
            ContractDeliveryRequest.Create request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contractDeliveryService.createContractDelivery(request, getAuthenticatedUserEmail()));
    }

    // Get all delivery by contract id
    @GetMapping("/contract/{contractId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'LAWYER', 'ADMIN')")
    public ResponseEntity<List<ContractDeliveryResponse.Create>> getAllDeliveriesByContractId(
            @PathVariable("contractId") UUID contractId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractDeliveryService.getAllContractDeliveriesByContractId(contractId, getAuthenticatedUserEmail()));
    }

    // Get in depth delivery info by delivery id (includes files and notes)
    @GetMapping("/{deliveryId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'LAWYER')")
    public ResponseEntity<ContractDeliveryResponse.AllInfo> getDeliveryById(
            @PathVariable("deliveryId") UUID contractDeliveryId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractDeliveryService.getContractDeliveryByDeliveryId(contractDeliveryId, getAuthenticatedUserEmail()));
    }

    @PatchMapping("/{contractDeliveryId}/request-revision")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ContractDeliveryResponse.Revision> requestRevision(
            @PathVariable("contractDeliveryId") UUID contractDeliveryId,
            @Valid
            @RequestBody ContractDeliveryRequest.Revision request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractDeliveryService.requestRevision(contractDeliveryId, request, getAuthenticatedUserEmail()));
    }
}
