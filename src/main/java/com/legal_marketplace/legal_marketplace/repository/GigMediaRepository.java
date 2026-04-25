package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.entity.GigMedia;
import com.legal_marketplace.legal_marketplace.entity.GigMediaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GigMediaRepository extends JpaRepository<GigMedia, GigMediaId> {
    List<GigMedia> findByGigId(UUID gigId);

    UUID gig(Gig gig);
}
