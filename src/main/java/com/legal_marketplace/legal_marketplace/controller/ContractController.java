package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.ContractRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractResponse;
import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
import com.legal_marketplace.legal_marketplace.service.ContractService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {

    /**
     * Get authenticated user email from SecurityContext
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    private final ContractService contractService;

    @PostMapping("") // Create Contract
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ContractResponse.Create> createContract(
            @Valid
            @RequestBody
            ContractRequest.Crete request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contractService.createContract(request, getAuthenticatedUserEmail()));
    }

    @GetMapping("/me/{role}") // Get contracts for authenticated user based on role (client or lawyer)
    @PreAuthorize("hasAnyRole('CLIENT', 'LAWYER')")
    public ResponseEntity<List<ContractResponse.ContractView>> getMyContracts(
            @Pattern(regexp = "(?i)^(CLIENT|LAWYER)$", message = "role must be CLIENT or LAWYER")
            @PathVariable("role") String role
    ) {
        System.out.println(role);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.getMyContracts(getAuthenticatedUserEmail(), role.toUpperCase()));
    }


    @GetMapping("/{contractId}")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT', 'ADMIN')")
    public ResponseEntity<ContractResponse.ContractExtendedView> getContract(
            @PathVariable("contractId") UUID contractId
    ) {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        Boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.getContractById(contractId, getAuthenticatedUserEmail(), isAdmin));
    }

    // Accept contract by lawyer, only if contract is in PENDING status and the authenticated user is the assigned lawyer
    @PatchMapping("/{contractId}/accept")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<ContractStatus> acceptContract(
            @PathVariable("contractId") UUID contractId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.acceptContract(contractId, getAuthenticatedUserEmail()));
    }

    // Reject contract by lawyer, only if contract is in PENDING status and the authenticated user is the assigned lawyer
    @PatchMapping("/{contractId}/reject")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<ContractStatus> rejectContract(
            @PathVariable("contractId") UUID contractId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.rejectContract(contractId, getAuthenticatedUserEmail()));
    }

    // Update to in Progress by lawyer
    @PatchMapping("/{contractId}/in-progress")
    @PreAuthorize("hasRole('LAWYER')")
    public  ResponseEntity<ContractStatus> inProgressContract(
            @PathVariable("contractId") UUID contractId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.contractInProgress(contractId, getAuthenticatedUserEmail()));
    }

    // Get All info about the contract
    @PatchMapping("/{contractId}/cancel")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT')")
    public ResponseEntity<ContractResponse.ContractExtendedView> cancelContract(
            @Valid
            @RequestBody
            ContractRequest.Cancel request,
            @PathVariable("contractId") UUID contractId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.cancelContract(request, contractId, getAuthenticatedUserEmail()));
    }

    // Complete the contract by client, only if contract is in IN_PROGRESS status and the authenticated user is the assigned client
    @PatchMapping("/{contractId}/complete")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ContractStatus> completeContract(
            @PathVariable("contractId") UUID contractId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.completeContract(contractId, getAuthenticatedUserEmail()));
    }
}
