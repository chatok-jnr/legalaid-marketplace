package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.GigReviewRequest;
import com.legal_marketplace.legal_marketplace.dto.response.GigReviewResponse;
import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.entity.GigReview;
import com.legal_marketplace.legal_marketplace.entity.GigReviewId;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.exception.GigExceptions;
import com.legal_marketplace.legal_marketplace.exception.GigReviewExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.GigRepository;
import com.legal_marketplace.legal_marketplace.repository.GigReviewRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.service.GigReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.classmate.AnnotationOverrides.builder;

@Slf4j
@Service
@RequiredArgsConstructor
public class GigReviewServiceImpl implements GigReviewService {

    private final UserRepository userRepository;
    private final GigRepository gigRepository;
    private final GigReviewRepository gigReviewRepository;

    @Override
    public GigReviewResponse createGigReview(GigReviewRequest.Create request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());

        if(gigRepository.existsById(request.getGigId())) {
            throw new GigExceptions.GigNotFoundException();
        }

        GigReview gigReview = createDtoToEntity(request, user.getId());
        if(gigReviewRepository.existsById(gigReview.getId())) {
            throw new GigReviewExceptions.DuplicateReviewException();
        }

        gigReview.setCreated_at(OffsetDateTime.now());
        gigReview.setUpdated_at(OffsetDateTime.now());

        gigReviewRepository.save(gigReview);
        return createEntityToDto(gigReview);
    }

    @Override
    public GigReviewResponse updateGigReview(GigReviewRequest.Create request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());

        if(gigRepository.existsById(request.getGigId())) {
            throw new GigExceptions.GigNotFoundException();
        }

        GigReview gigReview = createDtoToEntity(request, user.getId());
        gigReview.setUpdated_at(OffsetDateTime.now());
        gigReviewRepository.save(gigReview);
        return createEntityToDto(gigReview);
    }

    @Override
    public void deleteGigReview(String email, UUID gigId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());
        GigReviewId reviewId = GigReviewId.builder()
                .gigId(gigId)
                .userId(user.getId())
                .build();
        GigReview gigReview = gigReviewRepository.findById(reviewId)
                .orElseThrow(() -> new GigReviewExceptions.GigReviewNotFoundException());

        gigReviewRepository.delete(gigReview);
    }

    @Override
    public GigReviewResponse getMyReviews(String email, UUID gigId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());
        GigReviewId reviewId = GigReviewId.builder()
                .gigId(gigId)
                .userId(user.getId())
                .build();
        GigReview myReview = gigReviewRepository.findById(reviewId)
                .orElseThrow(() -> new GigReviewExceptions.GigReviewNotFoundException());

        return createEntityToDto(myReview);
    }

    @Override
    public List<GigReviewResponse> getGigReviews(UUID gigId) {
        if(!gigReviewRepository.existsByGigId(gigId)) {
            throw new GigExceptions.GigNotFoundException();
        }

        List<GigReview> gigReviews = gigReviewRepository.findByGigId(gigId);

        List<GigReviewResponse> gigReviewResponses = new ArrayList<>();
        for(int i = 0; i < gigReviews.size(); i++) {
            gigReviewResponses.add(createEntityToDto(gigReviews.get(i)));
        }
        return gigReviewResponses;
    }

    GigReview createDtoToEntity(GigReviewRequest.Create request, UUID reviewerId) {
        GigReviewId gigReviewId = GigReviewId.builder()
                .gigId(request.getGigId())
                .userId(reviewerId)
                .build();

        return GigReview.builder()
                .id(gigReviewId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
    }
    GigReviewResponse createEntityToDto(GigReview gigReview) {
        return GigReviewResponse.builder()
                .gigId(gigReview.getId().getGigId())
                .userId(gigReview.getId().getUserId())
                .rating(gigReview.getRating())
                .comment(gigReview.getComment())
                .createdAt(gigReview.getCreated_at())
                .updatedAt(gigReview.getUpdated_at())
                .build();
    }
}
