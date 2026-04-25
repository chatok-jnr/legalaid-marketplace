package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.GigReviewRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigResponse;
import com.legal_marketplace.legal_marketplace.dto.response.GigReviewResponse;

import java.util.List;
import java.util.UUID;

public interface GigReviewService {
    GigReviewResponse createGigReview(GigReviewRequest.Create request, String email);
    GigReviewResponse updateGigReview(GigReviewRequest.Create request, String email);
    void deleteGigReview(String email, UUID gigId);

    GigReviewResponse getMyReviews(String email, UUID gigId);
    List<GigReviewResponse> getGigReviews(UUID gigId);
}
