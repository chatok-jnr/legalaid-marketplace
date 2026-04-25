package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.GigMediaRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigMediaResponse;

import java.util.List;
import java.util.UUID;

public interface GigMediaService {
    GigMediaResponse createGigMedia(GigMediaRequest.Create request, String userEmail);
    GigMediaResponse updateGigMedia(GigMediaRequest.Update request, UUID gigId, int serialNo, String userEmail);
    List<GigMediaResponse> getGigMediaByGigId(UUID gigId);
    GigMediaResponse getGigMediaById(UUID gigId, int serialNo);
    void deleteGigMediaById(UUID gigId, int serialNo, String userEmail);
    void deleteAllGidMediaByGigId(UUID gigId, String userEmail);
}
