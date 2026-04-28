package com.legal_marketplace.legal_marketplace.repository.projectiions;

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
