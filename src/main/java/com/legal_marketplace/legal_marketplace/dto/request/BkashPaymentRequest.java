package com.legal_marketplace.legal_marketplace.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

public class BkashPaymentRequest {
    @Data
    public static class Create {
        @NotNull(message = "Contract payment ID is required")
        private UUID contractPaymentId;

        @NotBlank(message = "Sender number is required")
        @Size(max = 15, message = "Sender number must be at most 15 characters")
        @Pattern(regexp = "^(?:\\+?88)?01[3-9]\\d{8}$", message = "Sender number must be a valid Bangladeshi mobile number")
        private String senderNumber;

//        @NotBlank(message = "Receiver number is required")
//        @Size(max = 15, message = "Receiver number must be at most 15 characters")
//        @Pattern(regexp = "^(?:\\+?88)?01[3-9]\\d{8}$", message = "Receiver number must be a valid Bangladeshi mobile number")
//        private String receiverNumber;

        @NotBlank(message = "Transaction ID is required")
        @Size(max = 30, message = "Transaction ID must be at most 30 characters")
        private String transactionId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Amount must fit NUMERIC(12,2)")
        private BigDecimal amount;
    }

    @Data
    public static class Reject {
        @NotBlank(message = "Rejection reason is required")
        @Size(max = 5000, message = "Rejection reason must be at most 5000 characters")
        private String rejectionReason;
    }
}
