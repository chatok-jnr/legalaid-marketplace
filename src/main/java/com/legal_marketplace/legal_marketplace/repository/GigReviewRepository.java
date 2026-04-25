package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.GigReview;
import com.legal_marketplace.legal_marketplace.entity.GigReviewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GigReviewRepository extends JpaRepository<GigReview, GigReviewId> {
    List<GigReview> findByGigId(UUID gigId);
    boolean existsById(GigReviewId gigReviewId);
    boolean existsByGigId(UUID gigId);
}
