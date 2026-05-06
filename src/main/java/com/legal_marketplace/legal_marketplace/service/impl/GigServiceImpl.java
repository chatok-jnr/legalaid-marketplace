package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.GigRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigResponse;
import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.exception.GigExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.GigRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.repository.projections.PublicGigView;
import com.legal_marketplace.legal_marketplace.repository.projections.PublicGigViewInDetails;
import com.legal_marketplace.legal_marketplace.service.GigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GigServiceImpl implements GigService {
    private final UserRepository userRepository;
    private final GigRepository gigRepository;
    private final ObjectMapper objectMapper;

    // Create Gig
    @Override
    public GigResponse.MyGig createGig(GigRequest.CreateGig request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        Gig gig = createDtoToEntity(request);
        gig.setLawyerId(user.getId());

        gigRepository.save(gig);
        return mapToMyGig(gig);
    }

    // Update Gig
    @Override
    public GigResponse.MyGig updateGig(GigRequest.UpdateGig request, UUID id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        Gig gig = gigRepository.findById(id)
                .orElseThrow(GigExceptions.GigNotFoundException::new);

        if(!gig.getLawyerId().equals(user.getId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        if(request.getTitle() != null) gig.setTitle(request.getTitle());
        if(request.getMaxRevision() != 0) gig.setMaxRevision(request.getMaxRevision());
        if(request.getMinPrice() != null) gig.setMinPrice(request.getMinPrice());
        if(request.getAboutThisGig() != null) gig.setAboutThisGig(request.getAboutThisGig());
        if(request.isPublic() != gig.isPublic()) gig.setPublic(request.isPublic());
        gig.setUpdatedAt(OffsetDateTime.now());

        gigRepository.save(gig);

        return mapToMyGig(gig);
    }

    // Get My Gigs
    @Override
    public List<GigResponse.MyGig> myGigs(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserExceptions.UserNotFoundException::new);
        List<Gig> gigs = gigRepository.findByLawyerId(user.getId());

        List<GigResponse.MyGig> myGigs = new ArrayList<>();
        for (Gig gig : gigs) myGigs.add(mapToMyGig(gig));
        return myGigs;
    }

    // Delete my gig
    @Override
    public void deleteGig(UUID id, String email) {
        Gig gig = gigRepository.findById(id)
                .orElseThrow(GigExceptions.GigNotFoundException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        if(!user.getId().equals(gig.getLawyerId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        gigRepository.delete(gig);
    }


    // Get All Public Gigs
    @Override
    public Page<GigResponse.OthersGig> getAllPublicGigs(Pageable pageable) {
        Page<PublicGigView> gigPage = gigRepository.findAllPublicGigs(pageable);
        return gigPage.map(row -> {
            List<GigResponse.MediaInfoForPublicGig> mediaList = parseMediaFiles(row.getMediaFiles());

            return GigResponse.OthersGig.builder()
                    .id(row.getId())
                    .title(row.getTitle())
                    .lawyerId(row.getLawyerId())
                    .minPrice(row.getMinPrice())
                    .lawyerName(row.getFullName())
                    .lawyerProfilePicUrl(row.getProfilePicUrl())
                    .allMedia(mediaList)
                    .build();
        });
    }

    @Override
    public GigResponse.OthersGigDetails getPublicGigDetailsByGigId(UUID gigId) {
        PublicGigViewInDetails gigDetails = gigRepository.findPublicGigDetailsByGigId(gigId)
                .orElseThrow(GigExceptions.GigNotFoundException::new);

        PublicGigView gigBasicDetails = gigRepository.findPublicGigBasicByGigId(gigId)
                .orElseThrow(GigExceptions.GigNotFoundException::new);

        return GigResponse.OthersGigDetails.builder()
                .gigBasicInfo(GigResponse.OthersGig.builder()
                        .id(gigBasicDetails.getId())
                        .title(gigBasicDetails.getTitle())
                        .allMedia(parseMediaFiles(gigBasicDetails.getMediaFiles()))
                        .lawyerId(gigBasicDetails.getLawyerId())
                        .minPrice(gigBasicDetails.getMinPrice())
                        .lawyerName(gigBasicDetails.getFullName())
                        .lawyerProfilePicUrl(gigBasicDetails.getProfilePicUrl())
                        .build())
                .barNumber(gigDetails.getBar_number())
                .bio(gigDetails.getBio())
                .maxRevision(gigDetails.getMaxRevision())
                .specializations(gigDetails.getSpecializations())
                .yearsExperience(gigDetails.getYears_experience())
                .memberSince(gigDetails.getMember_since())
                .aboutThisGig(gigDetails.getAbout_this_gig())
                .build();
    }



    // --------------------------------------------------------------------------------
     // Functions ---------------------------------------------------------------------
    // --------------------------------------------------------------------------------

    private Gig createDtoToEntity(GigRequest.CreateGig request) {
        return Gig.builder()
                .title(request.getTitle())
                .minPrice(request.getMinPrice())
                .maxRevision(request.getMaxRevision())
                .aboutThisGig(request.getAboutThisGig())
                .isPublic(request.isPublic())
                .build();
    }
    private GigResponse.MyGig mapToMyGig(Gig gig) {
        return GigResponse.MyGig.builder()
                .id(gig.getId())
                .title(gig.getTitle())
                .lawyerId(gig.getLawyerId())
                .minPrice(gig.getMinPrice())
                .maxRevision(gig.getMaxRevision())
                .aboutThisGig(gig.getAboutThisGig())
                .isPublic(gig.isPublic())
                .createdAt(gig.getCreatedAt())
                .updatedAt(gig.getUpdatedAt())
                .build();
    }

    // Parse media files JSON to List of MediaInfoForPublicGig
    private List<GigResponse.MediaInfoForPublicGig> parseMediaFiles(String mediaFilesJson) {
        try {
            return (mediaFilesJson == null || mediaFilesJson.isBlank())
                    ? List.of()
                    : objectMapper.readValue(
                    mediaFilesJson,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error parsing media files JSON: {}", e.getMessage());
            return List.of(); // Return empty list if parsing fails
        }
    }
}
