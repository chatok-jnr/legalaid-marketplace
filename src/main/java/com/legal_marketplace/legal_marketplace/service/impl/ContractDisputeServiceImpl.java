package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDisputeRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDisputeResponse;
import com.legal_marketplace.legal_marketplace.entity.Contract;
import com.legal_marketplace.legal_marketplace.entity.ContractDispute;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import com.legal_marketplace.legal_marketplace.exception.ContractDisputeExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.ContractDisputeRepository;
import com.legal_marketplace.legal_marketplace.repository.ContractRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.service.ContractDisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractDisputeServiceImpl implements ContractDisputeService {
    private final ContractDisputeRepository contractDisputeRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;


    @Override
    public ContractDisputeResponse.Create create(ContractDisputeRequest.Create request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if(!user.getId().equals(contract.getClientId()) && !user.getId().equals(contract.getLawyerId())){
            throw new UserExceptions.AccessDeniedException();
        }

        if(contractDisputeRepository.existsByContractId(request.getContractId())){
            throw new ContractDisputeExceptions.AlreadyExists();
        }

        ContractDispute contractDispute = contractDisputeRepository.save(
                ContractDispute.builder()
                        .contract(contract)
                        .disputeStatus(DisputeStatus.OPEN)
                        .disputeReason(request.getDisputeReason())
                        .disputeOpenedBy(user.getId())
                        .disputeOpenedAt(Instant.now())
                        .build()
        );


        return ContractDisputeResponse.Create.builder()
                .disputeId(contractDispute.getId())
                .contractId(contractDispute.getContract().getId())
                .disputeReason(contractDispute.getDisputeReason())
                .disputeStatus(contractDispute.getDisputeStatus())
                .disputeOpenedBy(contractDispute.getDisputeOpenedBy())
                .build();
    }


}
