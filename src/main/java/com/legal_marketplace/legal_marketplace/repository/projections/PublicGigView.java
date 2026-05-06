package com.legal_marketplace.legal_marketplace.repository.projections;

import java.util.UUID;

public interface PublicGigView {
    UUID getId();
    String getTitle();
    UUID getLawyerId();
    int getMinPrice();
    String getMediaFiles();
    String getFullName();
    String getProfilePicUrl();
}
