package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.dto.request.GigReviewRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigReviewResponse;
import com.legal_marketplace.legal_marketplace.service.GigReviewService;
import io.jsonwebtoken.security.PublicJwkBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gigs/reviews")
public class GigReviewController {

    private final GigReviewService gigReviewService;

    /**
        Get Authenticated user email from SecurityContext
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }


    @PostMapping("/")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT')")
    public ResponseEntity<GigReviewResponse> createGigReview(
            @Valid
            @RequestBody
            GigReviewRequest.Create request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gigReviewService.createGigReview(request, getAuthenticatedUserEmail()));

    }

    @PatchMapping("/")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLIENT')")
    public ResponseEntity<GigReviewResponse> updateGigReview(
            @Valid
            @RequestBody
            GigReviewRequest.Create request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gigReviewService.updateGigReview(request, getAuthenticatedUserEmail()));
    }


}
