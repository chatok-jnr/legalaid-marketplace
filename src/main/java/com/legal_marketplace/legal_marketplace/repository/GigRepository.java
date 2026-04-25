package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.entity.GigMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GigRepository extends JpaRepository<Gig, UUID> {
    List<Gig> findByLawyerId(UUID lawyerId);
}
