package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.GigMediaRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigMediaResponse;
import com.legal_marketplace.legal_marketplace.service.GigMediaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
@RequiredArgsConstructor
@RequestMapping("/api/gigs/media")
public class GigMediaController {

    private final GigMediaService gigMediaService;

    /*
       Get Authenticated user email from the security context
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /*
        Create Gig media - requires LAWYER role
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<GigMediaResponse> create(
            @Valid
            @RequestBody
            GigMediaRequest.Create request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gigMediaService.createGigMedia(request, getAuthenticatedUserEmail()));
    }

    /*
    Update Gig media - requires LAWYER role
     */
    @PreAuthorize("hasRole('LAWYER')")
    @PatchMapping("{gigId}/{serialNo}")
    public ResponseEntity<GigMediaResponse> updateGigMediaById(
            @Valid
            @RequestBody
            GigMediaRequest.Update request,
            @PathVariable("gigId") UUID gigId,
            @Min(1)
            @PathVariable("serialNo") int serialNo
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigMediaService.updateGigMedia(request, gigId, serialNo, getAuthenticatedUserEmail()));
    }


    /*
    Get gig media by gig id
     */
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT', 'ADMIN')")
    @GetMapping("/{gigId}/{serialNo}")
    public ResponseEntity<GigMediaResponse> getGigMediaById(
            @PathVariable("gigId") UUID gigId,
            @Min(1)
            @PathVariable("serialNo") int serialNo
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigMediaService.getGigMediaById(gigId, serialNo));
     }

    /*
    Get gig media by gig id
     */
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT', 'ADMIN')")
    @GetMapping("/{gigId}")
    public ResponseEntity<List<GigMediaResponse>> getGigMediaById(
            @PathVariable("gigId") UUID gigId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigMediaService.getGigMediaByGigId(gigId));
    }

    /*
    Delete gig media by id - requires LAWYER role & ADMIN role
     */
    @PreAuthorize("hasAnyRole('LAWYER', 'ADMIN')")
    @DeleteMapping("/{gigId}/{serialNo}")
    public ResponseEntity<Void> deleteGigMeidaById(
            @PathVariable("gigId") UUID gigId,
            @Min(1)
            @PathVariable("serialNo") int serialNo
    ) {
        gigMediaService.deleteGigMediaById(gigId, serialNo, getAuthenticatedUserEmail());
        return ResponseEntity.noContent().build();
    }

    /*
    Delete all gig media by gig id - requires LAWYER role & ADMIN role
     */
    @PreAuthorize("hasAnyRole('LAWYER', 'ADMIN')")
    @DeleteMapping("/{gigId}")
    public ResponseEntity<Void> deleteGigMeidaById(
            @PathVariable("gigId") UUID gigId
    ) {
        gigMediaService.deleteAllGidMediaByGigId(gigId, getAuthenticatedUserEmail());
        return ResponseEntity.noContent().build();
    }
}
