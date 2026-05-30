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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
        bkashPayment.setVerifiedAt(Instant.now());
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
        bkashPayment.setVerifiedAt(Instant.now());
        bkashPayment.setRejectionReason(request.getRejectionReason());

        return mapVerification(bkashPaymentRepository.save(bkashPayment));
    }

    @Override
    public List<BkashPaymentResponse.BasicView> getAllByStatusForAdmin(String status, int page, int size, String adminEmail) {
        Page<BkashPayment> bkashPaymentsPage = Page.empty();

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        if(!status.equalsIgnoreCase(BkashPaymentStatus.PENDING.name())) {
            Pageable pageable = PageRequest.of(page, size,  Sort.by(Sort.Direction.DESC, "createdAt"));
            bkashPaymentsPage = bkashPaymentRepository.findByStatusAndVerifiedBy(BkashPaymentStatus.valueOf(status), admin.getId(), pageable);
        } else {
            Pageable pageable = PageRequest.of(page, size,  Sort.by(Sort.Direction.DESC, "createdAt"));
            bkashPaymentsPage = bkashPaymentRepository.findByStatusAndVerifiedByIsNull(BkashPaymentStatus.valueOf(status), pageable);
        }

        List<BkashPaymentResponse.BasicView> res = new ArrayList<>();
        for(BkashPayment bkashPayment: bkashPaymentsPage.getContent()) {
            res.add(
                    BkashPaymentResponse.BasicView.builder()
                            .id(bkashPayment.getId())
                            .amount(bkashPayment.getAmount())
                            .method("BKASH")
                            .submitted(bkashPayment.getCreatedAt())
                            .status(bkashPayment.getStatus())
                            .build()
            );
        }

        return res;
    }

    @Override
    public BkashPaymentResponse.ExtendedView getPaymentDetailsById(UUID id) {
        BkashPayment bkashPayment = bkashPaymentRepository.findById(id)
                .orElseThrow(BkashPaymentExceptions.NotFound::new);

        return BkashPaymentResponse.ExtendedView.builder()
                .id(bkashPayment.getId())
                .platformFeeAmount(bkashPayment.getContractPayment().getPlatformFeeAmount())
                .lawyerPayoutAmount(bkashPayment.getContractPayment().getLawyerPayoutAmount())
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

    @Override
    public BkashPaymentResponse.ExtendedView updStatus(UUID id, BkashPaymentRequest.UpdateStatus request, String adminEmail) {
        BkashPayment payment = bkashPaymentRepository.findById(id)
                .orElseThrow(BkashPaymentExceptions.NotFound::new);
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        if(!payment.getStatus().equals(BkashPaymentStatus.PENDING)) {
            throw new BkashPaymentExceptions.BadRequest("Only pending payments can be updated");
        }

        BkashPaymentStatus reqStatus = BkashPaymentStatus.valueOf(request.getStatus());
        ContractPayment contractPayment = payment.getContractPayment();



        if(reqStatus.equals(BkashPaymentStatus.VERIFIED)) {
            payment.setVerifiedBy(admin.getId());
            payment.setVerifiedAt(Instant.now());
            payment.setStatus(BkashPaymentStatus.VERIFIED);
            contractPayment.setPaymentStatus(PaymentStatus.HELD);
            contractPayment.setPaidAt(Instant.now());
            contractPayment.setPaymentReference("bKash");
            paymentRepository.save(contractPayment);
        } else if(reqStatus.equals(BkashPaymentStatus.REJECTED)) {
            if(request.getRejectionReason() == null) {
                throw new BkashPaymentExceptions.BadRequest("Rejection reason can't be null");
            }
            payment.setRejectionReason(request.getRejectionReason());
            payment.setStatus(reqStatus);
        } else {
            throw new BkashPaymentExceptions.BadRequest("Invalid request");
        }

        bkashPaymentRepository.save(payment);

        return BkashPaymentResponse.ExtendedView.builder()
                .id(payment.getId())
                .platformFeeAmount(payment.getContractPayment().getPlatformFeeAmount())
                .lawyerPayoutAmount(payment.getContractPayment().getLawyerPayoutAmount())
                .senderNumber(payment.getSenderNumber())
                .receiverNumber(payment.getReceiverNumber())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .verifiedBy(payment.getVerifiedBy())
                .verifiedAt(payment.getVerifiedAt())
                .rejectionReason(payment.getRejectionReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
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
