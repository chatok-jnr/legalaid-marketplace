package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.GigRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigResponse;
import com.legal_marketplace.legal_marketplace.service.GigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gigs")
@RequiredArgsConstructor
public class GigController {

    private final GigService gigService;

    /**
     * Get authenticated user email from SecurityContext
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    // Create gig - requires LAWYER role
    @PostMapping("/me")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<GigResponse.MyGig> createGig(
            @Valid @RequestBody GigRequest.CreateGig request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gigService.createGig(request, getAuthenticatedUserEmail()));
    }

    // Update Gig - requires LAWYER role
    @PatchMapping("/me/{id}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<GigResponse.MyGig> updateGig(
            @Valid @RequestBody GigRequest.UpdateGig request,
            @PathVariable("id") UUID gigId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigService.updateGig(request, gigId, getAuthenticatedUserEmail()));
    }

    // Get My gigs - requires LAWYER role
    @GetMapping("/me")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<List<GigResponse.MyGig>> getMyGigs() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigService.myGigs(getAuthenticatedUserEmail()));
    }

    // Delete my Gigs - requires LAWYER role
    @DeleteMapping("/me/{id}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Void> deleteGig(@PathVariable("id") UUID gigId) {
        gigService.deleteGig(gigId, getAuthenticatedUserEmail());
        return ResponseEntity.noContent().build();
    }

    // Get all Public gigs
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT', 'ADMIN')")
    @GetMapping("/public")
    public ResponseEntity<Page<GigResponse.OthersGig>> getPublicGigs(
            @PageableDefault(size = 30, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigService.getAllPublicGigs(pageable));
    }

    // Get public gig in details by gig id
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT', 'ADMIN')")
    @GetMapping("/public/{gigId}")
    public ResponseEntity<GigResponse.OthersGigDetails> getPublicGigDetailsByGigId(
            @PathVariable("gigId") UUID gigId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigService.getPublicGigDetailsByGigId(gigId));
    }
}



