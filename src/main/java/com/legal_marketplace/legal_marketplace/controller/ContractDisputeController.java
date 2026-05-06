package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDisputeRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDisputeResponse;
import com.legal_marketplace.legal_marketplace.service.ContractDisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

}
