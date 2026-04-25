package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.GigRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigResponse;
import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.exception.GigExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.GigRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.service.GigService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    // Create Gig
    @Override
    public GigResponse.MyGig createGig(GigRequest.CreateGig request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());

        Gig gig = createDtoToEntity(request);
        gig.setLawyerId(user.getId());

        gigRepository.save(gig);
        return mapToMyGig(gig);
    }

    // Update Gig
    @Override
    public GigResponse.MyGig updateGig(GigRequest.UpdateGig request, UUID id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());

        Gig gig = gigRepository.findById(id)
                .orElseThrow(() -> new GigExceptions.GigNotFoundException());

        if(!gig.getLawyerId().equals(user.getId())) {
            throw new UserExceptions.AccessDeniedException();
        }

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
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());
        List<Gig> gigs = gigRepository.findByLawyerId(user.getId());
        if(gigs.isEmpty()) {
            throw new GigExceptions.GigNotFoundException();
        }

        List<GigResponse.MyGig> myGigs = new ArrayList<>();
        for(int i = 0; i < gigs.size(); i++) myGigs.add(mapToMyGig(gigs.get(i)));
        return myGigs;
    }

    @Override
    public void deleteGig(UUID id, String email) {
        Gig gig = gigRepository.findById(id)
                .orElseThrow(() -> new GigExceptions.GigNotFoundException());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());

        if(!user.getId().equals(gig.getLawyerId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        gigRepository.delete(gig);
    }

    private Gig createDtoToEntity(GigRequest.CreateGig request) {
        return Gig.builder()
                .title(request.getTitle())
                .minPrice(request.getMinPrice())
                .aboutThisGig(request.getAboutThisGig())
                .isPublic(request.isPublic())
                .build();
    }
    private Gig updateDtoToEntity(GigRequest.UpdateGig request) {
        return Gig.builder()
                .title(request.getTitle())
                .minPrice(request.getMinPrice())
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
                .aboutThisGig(gig.getAboutThisGig())
                .isPublic(gig.isPublic())
                .createdAt(gig.getCreatedAt())
                .updatedAt(gig.getUpdatedAt())
                .build();
    }
}
