package com.legal_marketplace.legal_marketplace.entity;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Data
public class GigMediaId implements Serializable {
    private UUID gigId;
    @Min(1)
    private int serialNo;
}
