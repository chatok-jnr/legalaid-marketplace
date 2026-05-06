package com.legal_marketplace.legal_marketplace.repository;

import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.repository.projectiions.PublicGigView;
import com.legal_marketplace.legal_marketplace.repository.projectiions.PublicGigViewInDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GigRepository extends JpaRepository<Gig, UUID> {
    List<Gig> findByLawyerId(UUID lawyerId);

    // All Public Gig basic info with pagination
    @Query(value = """
        SELECT 
            g.id as id, 
            g.title as title, 
            g.lawyer_id as lawyerId, 
            g.min_price as minPrice,
            u.full_name as fullName, 
            u.profile_pic_url as profilePicUrl,
            COALESCE(
                jsonb_agg(
                    jsonb_build_object(
                        'url', gm.url,
                        'serial_no', gm.serial_no
                    )
                ) FILTER (WHERE gm.gig_id IS NOT NULL),
                '[]'::jsonb
            ) AS mediaFiles
        FROM gigs g
        LEFT JOIN gig_media gm ON g.id = gm.gig_id
        LEFT JOIN users u ON g.lawyer_id = u.id
        WHERE g.is_public = TRUE
        GROUP BY g.id, g.title, g.lawyer_id, g.min_price, u.id, u.full_name, u.profile_pic_url
        ORDER BY g.id
    """,
    countQuery = """
        SELECT COUNT(DISTINCT g.id)
        FROM gigs g 
        WHERE g.is_public = TRUE
    """,
    nativeQuery = true)
    Page<PublicGigView> findAllPublicGigs(Pageable pageable);

    // Get a specific gig basic info by gigId
    @Query(value = """
        SELECT 
            g.id as id, 
            g.title as title, 
            g.lawyer_id as lawyerId, 
            g.min_price as minPrice,
            u.full_name as fullName, 
            u.profile_pic_url as profilePicUrl,
            COALESCE(
                jsonb_agg(
                    jsonb_build_object(
                        'url', gm.url,
                        'serial_no', gm.serial_no
                    )
                ) FILTER (WHERE gm.gig_id IS NOT NULL),
                '[]'::jsonb
            ) AS mediaFiles
        FROM gigs g
        LEFT JOIN gig_media gm ON g.id = gm.gig_id
        LEFT JOIN users u ON g.lawyer_id = u.id
        WHERE g.is_public = TRUE AND g.id = :gigId
        GROUP BY g.id, g.title, g.lawyer_id, g.min_price, u.id, u.full_name, u.profile_pic_url
        ORDER BY g.id
    """,
    nativeQuery = true)
    Optional<PublicGigView> findPublicGigBasicByGigId(@Param("gigId")UUID gigId);

    // Get a specific gig details by gigId
    @Query(value = """
    SELECT\s
    	g.about_this_gig AS about_this_gig,
        g.max_revision AS maxRevision,
    	lp.bar_number AS bar_number,\s
    	lp.bio AS bio,\s
    	lp.specializations AS specializations,\s
    	lp.years_experience AS years_experience,
    	lp.created_at AS member_since
    FROM gigs g
    LEFT JOIN lawyer_profiles lp ON g.lawyer_id = lp.id
    WHERE g.id = :gigId AND g.is_public = TRUE
    """,
    nativeQuery = true)
    Optional<PublicGigViewInDetails> findPublicGigDetailsByGigId(@Param("gigId")UUID gigId);
}
