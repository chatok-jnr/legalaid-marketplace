package com.legal_marketplace.legal_marketplace.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jdk.dynalink.beans.StaticClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.print.Pageable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class GigResponse {

    @Data
    @Builder
    public static class MyGig {
        private UUID id;
        private UUID lawyerId;
        private String title;
        private int minPrice;
        private String aboutThisGig;
        private boolean isPublic;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

//    @Data
//    @Builder
//    public static class OthersGig {
//        private UUID id;
//        private UUID lawyerId;
//
//        private String title;
//        private int minPrice;
//        private String aboutThisGig;
//        List<GigMediaResponse> gigMediaResponses;
//        private OffsetDateTime updatedAt;
//    }
    // Others Gigs - Accessible to all users, includes lawyer's name and profile pic
    @Data
    @Builder
    public static class OthersGig {
        private String lawyerName;
        private String lawyerProfilePicUrl;

        private UUID id;
        private String title;
        private UUID lawyerId;
        private int minPrice;

        List<MediaInfoForPublicGig> allMedia;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaInfoForPublicGig{
        @JsonProperty("url")
        private String url;
        @JsonProperty("serial_no")
        private int serialNo;
    }

    @Data
    @Builder
    public static class OtherGig {
        private UUID id;
        private UUID lawyerId;
        private String title;
        private int minPrice;
        private String aboutThisGig;
        private OffsetDateTime updatedAt;
    }

    @Data
    @Builder
    public static class OthersGigDetails {
        private String aboutThisGig;
        private String lawyerEmail;
        private OffsetDateTime updatedAt;
    }

}
