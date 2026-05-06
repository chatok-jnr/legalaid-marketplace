package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.PaymentRequest;
import com.legal_marketplace.legal_marketplace.dto.response.PaymentResponse;
import com.legal_marketplace.legal_marketplace.entity.Contract;
import com.legal_marketplace.legal_marketplace.entity.ContractPayment;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus;
import com.legal_marketplace.legal_marketplace.exception.ContractExceptions;
import com.legal_marketplace.legal_marketplace.exception.PaymentExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.ContractRepository;
import com.legal_marketplace.legal_marketplace.repository.PaymentRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    public PaymentResponse.Create createPayment(PaymentRequest.Create request, String userEmail) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(ContractExceptions.NotFound::new);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        if(!user.getId().equals(contract.getClientId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        if(paymentRepository.existsByContractId(request.getContractId())) {
            throw new PaymentExceptions.PaymentExists();
        }

        ContractPayment payment = ContractPayment.builder()
                .contract(contract)
                .paymentStatus(PaymentStatus.UNPAID)
                .platformFeeAmount((int)(contract.getPriceAtHire() * 0.10))
                .platformFeePercent(BigDecimal.valueOf(10.00))
                .lawyerPayoutAmount(contract.getPriceAtHire() - (int)(contract.getPriceAtHire() * 0.10))
                .paymentReference(request.getPaymentReference())
                .build();

        ContractPayment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.Create.builder()
                .id(savedPayment.getId())
                .contractId(savedPayment.getContract().getId())
                .platformFeeAmount(savedPayment.getPlatformFeeAmount())
                .platformFeePercent(savedPayment.getPlatformFeePercent())
                .lawyerPayoutAmount(savedPayment.getLawyerPayoutAmount())
                .paymentStatus(savedPayment.getPaymentStatus())
                .paymentReference(savedPayment.getPaymentReference())
                .build();
    }
}
