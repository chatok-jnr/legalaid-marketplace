package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDeliveryFileRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryFileResponse;
import com.legal_marketplace.legal_marketplace.service.ContractDeliveryFileService;
import com.legal_marketplace.legal_marketplace.validation.NotEmptyFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class ContractDeliveryFileController {

    private final ContractDeliveryFileService contractDeliveryFileService;

    /** Get authenticated user email from SecurityContext */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    @PostMapping("/{deliveryId}/files")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<ContractDeliveryFileResponse.Create> createDeliveryFile(
            @Valid
            @RequestBody ContractDeliveryFileRequest.Create request,
            @PathVariable("deliveryId") UUID deliveryId
    ) {
        ContractDeliveryFileResponse.Create created = contractDeliveryFileService.createContractDeliveryFile(request, deliveryId, getAuthenticatedUserEmail());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    /**
     * Upload file to Cloudinary and register it for delivery
     * Frontend sends MultipartFile, we upload to Cloudinary, save metadata to DB
     */
    @PostMapping("/{deliveryId}/files/upload")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<?> uploadDeliveryFile(
            @NotEmptyFile
            @RequestParam("file") MultipartFile file,
            @PathVariable("deliveryId") UUID deliveryId
    ) {
        try {
            ContractDeliveryFileResponse.Create created = contractDeliveryFileService.uploadAndRegisterFile(
                    file,
                    deliveryId,
                    getAuthenticatedUserEmail()
            );
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(created);
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "File upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{deliveryId}/files/{fileId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Void> deleteDeliveryFile(
            @PathVariable("deliveryId") UUID deliveryId,
            @PathVariable("fileId") UUID fileId
    ) {
        contractDeliveryFileService.deleteContractDeliveryFile(deliveryId, fileId, getAuthenticatedUserEmail());
        return ResponseEntity.noContent().build();
    }
}
