package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.BkashPaymentRequest;
import com.legal_marketplace.legal_marketplace.dto.response.BkashPaymentResponse;
import com.legal_marketplace.legal_marketplace.entity.BkashPayment;
import com.legal_marketplace.legal_marketplace.entity.Contract;
import com.legal_marketplace.legal_marketplace.entity.ContractPayment;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.entity.enums.BkashPaymentStatus;
import com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus;
import com.legal_marketplace.legal_marketplace.exception.BkashPaymentExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.BkashPaymentRepository;
import com.legal_marketplace.legal_marketplace.repository.PaymentRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.service.BkashPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BkashPaymentServiceImpl implements BkashPaymentService {

    private final BkashPaymentRepository bkashPaymentRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BkashPaymentResponse.Create create(BkashPaymentRequest.Create request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        ContractPayment contractPayment = paymentRepository.findById(request.getContractPaymentId())
                .orElseThrow(BkashPaymentExceptions.NotFound::new);

        Contract contract = contractPayment.getContract();
        if (!user.getId().equals(contract.getClientId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        if (bkashPaymentRepository.existsByContractPaymentId(request.getContractPaymentId())) {
            throw new BkashPaymentExceptions.AlreadyExists();
        }

        if (bkashPaymentRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new BkashPaymentExceptions.DuplicateTransactionId();
        }

        BkashPayment bkashPayment = bkashPaymentRepository.save(
                BkashPayment.builder()
                        .contractPayment(contractPayment)
                        .senderNumber(request.getSenderNumber())
                        .receiverNumber("01971311958")
                        .transactionId(request.getTransactionId())
                        .amount(request.getAmount())
                        .status(BkashPaymentStatus.PENDING)
                        .build()
        );

        return BkashPaymentResponse.Create.builder()
                .id(bkashPayment.getId())
                .contractPaymentId(bkashPayment.getContractPayment().getId())
                .senderNumber(bkashPayment.getSenderNumber())
                .receiverNumber(bkashPayment.getReceiverNumber())
                .transactionId(bkashPayment.getTransactionId())
                .amount(bkashPayment.getAmount())
                .status(bkashPayment.getStatus())
                .createdAt(bkashPayment.getCreatedAt())
                .updatedAt(bkashPayment.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BkashPaymentResponse.Details getByContractPaymentId(UUID contractPaymentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        BkashPayment bkashPayment = bkashPaymentRepository.findByContractPaymentId(contractPaymentId)
                .orElseThrow(BkashPaymentExceptions.NotFound::new);

        Contract contract = bkashPayment.getContractPayment().getContract();
        if (!user.getId().equals(contract.getClientId()) && !user.getId().equals(contract.getLawyerId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        return mapDetails(bkashPayment);
    }

    @Override
    @Transactional
    public BkashPaymentResponse.Verification verify(UUID bkashPaymentId, String userEmail) {
        User admin = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        BkashPayment bkashPayment = bkashPaymentRepository.findById(bkashPaymentId)
                .orElseThrow(BkashPaymentExceptions.NotFound::new);

        if (bkashPayment.getStatus() != BkashPaymentStatus.PENDING) {
            throw new BkashPaymentExceptions.BadRequest("Only pending bKash payments can be verified");
        }

        ContractPayment contractPayment = bkashPayment.getContractPayment();
        contractPayment.setPaymentStatus(PaymentStatus.HELD);
        contractPayment.setPaidAt(Instant.now());
        contractPayment.setPaymentReference("bKash");
        paymentRepository.save(contractPayment);

        bkashPayment.setStatus(BkashPaymentStatus.VERIFIED);
        bkashPayment.setVerifiedBy(admin.getId());
        bkashPayment.setVerifiedAt(OffsetDateTime.now());
        bkashPayment.setRejectionReason(null);

        return mapVerification(bkashPaymentRepository.save(bkashPayment));
    }

    @Override
    @Transactional
    public BkashPaymentResponse.Verification reject(UUID contractPaymentId, BkashPaymentRequest.Reject request, String userEmail) {
        User admin = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        BkashPayment bkashPayment = bkashPaymentRepository.findByContractPaymentId(contractPaymentId)
                .orElseThrow(BkashPaymentExceptions.NotFound::new);

        if (bkashPayment.getStatus() != BkashPaymentStatus.PENDING) {
            throw new BkashPaymentExceptions.BadRequest("Only pending bKash payments can be rejected");
        }

        bkashPayment.setStatus(BkashPaymentStatus.REJECTED);
        bkashPayment.setVerifiedBy(admin.getId());
        bkashPayment.setVerifiedAt(OffsetDateTime.now());
        bkashPayment.setRejectionReason(request.getRejectionReason());

        return mapVerification(bkashPaymentRepository.save(bkashPayment));
    }



    // =============================================
    // ==================Functions==================
    // =============================================

    private BkashPaymentResponse.Details mapDetails(BkashPayment bkashPayment) {
        Contract contract = bkashPayment.getContractPayment().getContract();
        return BkashPaymentResponse.Details.builder()
                .id(bkashPayment.getId())
                .contractPaymentId(bkashPayment.getContractPayment().getId())
                .contractId(contract.getId())
                .clientId(contract.getClientId())
                .lawyerId(contract.getLawyerId())
                .senderNumber(bkashPayment.getSenderNumber())
                .receiverNumber(bkashPayment.getReceiverNumber())
                .transactionId(bkashPayment.getTransactionId())
                .amount(bkashPayment.getAmount())
                .status(bkashPayment.getStatus())
                .verifiedBy(bkashPayment.getVerifiedBy())
                .verifiedAt(bkashPayment.getVerifiedAt())
                .rejectionReason(bkashPayment.getRejectionReason())
                .createdAt(bkashPayment.getCreatedAt())
                .updatedAt(bkashPayment.getUpdatedAt())
                .build();
    }

    private BkashPaymentResponse.Verification mapVerification(BkashPayment bkashPayment) {
        return BkashPaymentResponse.Verification.builder()
                .id(bkashPayment.getId())
                .contractPaymentId(bkashPayment.getContractPayment().getId())
                .status(bkashPayment.getStatus())
                .verifiedBy(bkashPayment.getVerifiedBy())
                .verifiedAt(bkashPayment.getVerifiedAt())
                .rejectionReason(bkashPayment.getRejectionReason())
                .updatedAt(bkashPayment.getUpdatedAt())
                .build();
    }
}
