package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.GigMediaRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigMediaResponse;
import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.entity.GigMedia;
import com.legal_marketplace.legal_marketplace.entity.GigMediaId;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.exception.GigExceptions;
import com.legal_marketplace.legal_marketplace.exception.GigMediaExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.GigMediaRepository;
import com.legal_marketplace.legal_marketplace.repository.GigRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.service.CloudinaryService;
import com.legal_marketplace.legal_marketplace.service.GigMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GigMediaServiceImpl implements GigMediaService {

    private final GigMediaRepository gigMediaRepository;
    private final GigRepository gigRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // Create GigMedia
    @Override
    public GigMediaResponse createGigMedia(GigMediaRequest.Create request, String userEmail) {

        Gig gig = gigRepository.findById(request.getGigId())
                .orElseThrow(() -> new RuntimeException("Gig not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("user iD" + user.getId() + "\n" + gig.getLawyerId());

        if(!user.getId().equals(gig.getLawyerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if(gigMediaRepository.existsById(new GigMediaId(request.getGigId(), request.getSerialNo()))) {
            throw new GigMediaExceptions.ConflictException();
        }

        GigMedia gigMedia = createToEntity(request);
        gigMediaRepository.save(gigMedia);
        return entityToResponse(gigMedia);
    }

    // Update Gig media
    @Override
    public GigMediaResponse updateGigMedia(GigMediaRequest.Update request, UUID gigId, int serialNo, String userEmail) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new GigExceptions.GigNotFoundException());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());


        if(!gig.getLawyerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        GigMedia gigMedia = gigMediaRepository.findById(new GigMediaId(gigId, serialNo))
                .orElseThrow(() -> new GigMediaExceptions.NotFoundException());

        gigMedia.setId(new GigMediaId(gigId, request.getNewSerialNo()));
        gigMediaRepository.save(gigMedia);
        return entityToResponse(gigMedia);
    }

    // Get gig media by gig id
    @Override
    public List<GigMediaResponse> getGigMediaByGigId(UUID gigId) {
        List<GigMedia> gigMediaList = gigMediaRepository.findByGigId(gigId);

        List<GigMediaResponse> gigMediaResponses = new ArrayList<>();
        if(gigMediaList.isEmpty())  throw new GigMediaExceptions.NotFoundException();
        for(GigMedia u: gigMediaList)  gigMediaResponses.add(entityToResponse(u));

        return gigMediaResponses;
    }

    // Get gig media by id
    @Override
    public GigMediaResponse getGigMediaById(UUID gigId, int serialNo) {
        GigMedia gigMedia = gigMediaRepository.findById(new GigMediaId(gigId, serialNo))
                .orElseThrow(() -> new GigMediaExceptions.NotFoundException());

        return entityToResponse(gigMedia);
    }

    // Delete all gig media by gig id
    @Override
    public void deleteAllGidMediaByGigId(UUID gigId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new GigExceptions.GigNotFoundException());

        if(!gig.getLawyerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<GigMedia> gigMediaList = gigMediaRepository.findByGigId(gigId);

        for(GigMedia gigMedia: gigMediaList) {
            try{
                Map<?, ?> dltFromCloudinary = cloudinaryService.deleteFile(gigMedia.getPublicId(), gigMedia.getResourceType());
            } catch(java.io.IOException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        gigMediaRepository.deleteAll(gigMediaList);
    }

    // Delete a specific gig media by its id
    @Override
    public void deleteGigMediaById(UUID gigId, int serialNo, String userEmail) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new RuntimeException("Gig not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());

        if(!gig.getLawyerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }


        GigMedia gigMedia = gigMediaRepository.findById(new GigMediaId(gigId, serialNo))
                .orElseThrow(() -> new GigMediaExceptions.NotFoundException());

        try{
            Map<?, ?> dltFromCloudinary = cloudinaryService.deleteFile(gigMedia.getPublicId(), gigMedia.getResourceType());
        } catch(java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary Deleted Failed", ex);
        }

        gigMediaRepository.delete(gigMedia);
    }

    GigMedia createToEntity(GigMediaRequest.Create request) {
        GigMediaId gigMediaId = GigMediaId.builder()
                .gigId(request.getGigId())
                .serialNo(request.getSerialNo())
                .build();

        return GigMedia.builder()
                .id(gigMediaId)
                .url(request.getUrl())
                .publicId(request.getPublicId())
                .resourceType(request.getResourceType())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    GigMediaResponse entityToResponse(GigMedia request) {
        return GigMediaResponse.builder()
                .gigId(request.getId().getGigId())
                .serialNo(request.getId().getSerialNo())
                .url(request.getUrl())
                .publicId(request.getPublicId())
                .resourceType(request.getResourceType())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
