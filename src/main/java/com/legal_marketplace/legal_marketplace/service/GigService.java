package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.GigRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GigService {
    GigResponse.MyGig createGig(GigRequest.CreateGig request, String email);
    GigResponse.MyGig updateGig(GigRequest.UpdateGig request, UUID id, String email);
    List<GigResponse.MyGig> myGigs(String email);
    void deleteGig(UUID id, String email);
    Page<GigResponse.OthersGig> getAllPublicGigs(Pageable pageable);
    GigResponse.OthersGigDetails getPublicGigDetailsByGigId(UUID gigId);
}
