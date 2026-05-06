package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDisputeRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDisputeResponse;
import com.legal_marketplace.legal_marketplace.entity.Contract;
import com.legal_marketplace.legal_marketplace.entity.ContractDispute;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractDisputeServiceImpl implements ContractDisputeService {
    private final ContractDisputeRepository contractDisputeRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public ContractDisputeResponse.Create create(ContractDisputeRequest.Create request, String userEmail) {

        // Validate User and Contract
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found"));


        // Check if there's already an open dispute for this contract
        if(contractDisputeRepository.existsByContractId(request.getContractId())){
            throw new ContractDisputeExceptions.AlreadyExists();
        }

        // Check if user is either the client or the lawyer of the contract and if the contract is in a status that allows disputes
        if(contract.getClientId().equals(user.getId())) {
            if(!contract.getStatus().equals(ContractStatus.IN_PROGRESS) && !contract.getStatus().equals(ContractStatus.DELIVERED)){
                throw new ContractDisputeExceptions.BadRequest("Client can only dispute contracts that are in progress or delivered");
            }
        } else if(contract.getLawyerId().equals(user.getId())) {
            if(!contract.getStatus().equals(ContractStatus.IN_REVISION) && !contract.getStatus().equals(ContractStatus.DELIVERED)){
                throw new ContractDisputeExceptions.BadRequest("Lawyer can only dispute contracts that are in revision or delivered");
            }
        } else {
            throw new UserExceptions.AccessDeniedException();
        }

        // Create Contract Dispute
        ContractDispute contractDispute = contractDisputeRepository.save(
                ContractDispute.builder()
                        .contract(contract)
                        .disputeStatus(DisputeStatus.OPEN)
                        .disputeReason(request.getDisputeReason())
                        .disputeOpenedBy(user.getId())
                        .disputeOpenedAt(Instant.now())
                        .build()
        );

        // Update Contract Status to DISPUTED
        contract.setStatus(ContractStatus.DISPUTED);
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);

        // Return Response
        return ContractDisputeResponse.Create.builder()
                .disputeId(contractDispute.getId())
                .contractId(contractDispute.getContract().getId())
                .disputeReason(contractDispute.getDisputeReason())
                .disputeStatus(contractDispute.getDisputeStatus())
                .disputeOpenedBy(contractDispute.getDisputeOpenedBy())
                .build();
    }
}
