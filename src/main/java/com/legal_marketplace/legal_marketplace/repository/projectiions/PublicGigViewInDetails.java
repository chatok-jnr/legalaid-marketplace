package com.legal_marketplace.legal_marketplace.repository.projectiions;

import java.time.Instant;
import java.util.List;

public interface PublicGigViewInDetails {
    String getBar_number();
    String getBio();
    List<String> getSpecializations();
    int getYears_experience();
    int getMaxRevision();
    String getAbout_this_gig();
    Instant getMember_since();
}
