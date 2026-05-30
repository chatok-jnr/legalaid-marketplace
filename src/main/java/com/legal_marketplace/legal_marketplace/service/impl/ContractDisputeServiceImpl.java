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
import com.legal_marketplace.legal_marketplace.repository.projections.ContractDisputeBasicViewProjection;
import com.legal_marketplace.legal_marketplace.service.ContractDisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional
    public String assignAdmin(UUID disputeId, String adminEmail) {
        User user = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException());
        ContractDispute dispute = contractDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new ContractDisputeExceptions.NotFound());

        if(dispute.getAdminId() != null) {
            throw new ContractDisputeExceptions.BadRequest("Admin can not be assigned to dispute");
        }

        dispute.setAdminId(user.getId());
        dispute.setDisputeStatus(DisputeStatus.UNDER_REVIEW);
        contractDisputeRepository.save(dispute);

        return "Admin assigned to dispute successfully";
    }

    @Override
    public String resolveDispute(UUID disputeId, String resolution, String adminEmail) {
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new UserExceptions.UserNotFoundException());
            ContractDispute dispute = contractDisputeRepository.findById(disputeId)
                    .orElseThrow(() -> new ContractDisputeExceptions.NotFound());

            if(!dispute.getAdminId().equals(admin.getId())) {
                throw new ContractDisputeExceptions.BadRequest("You are not authorized to perform this action");
            }

            if(!dispute.getDisputeStatus().equals(DisputeStatus.UNDER_REVIEW)) {
                throw new ContractDisputeExceptions.BadRequest("Only disputes that are under review can be resolved");
            }

            dispute.setDisputeResolution(resolution);
            dispute.setDisputeResolvedAt(Instant.now());
            dispute.setDisputeStatus(DisputeStatus.RESOLVED);
            contractDisputeRepository.save(dispute);

            return "Dispute resolved successfully";
    }

    @Override
    public List<ContractDisputeResponse.BasicView> getDisputes(DisputeStatus disputeStatus, String adminEmail) {
        List<ContractDisputeBasicViewProjection> disputes = new ArrayList<>();
        if(disputeStatus.equals(disputeStatus.OPEN)) {
            disputes = contractDisputeRepository.findAllContractDispute(disputeStatus.name());
        } else {
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new UserExceptions.UserNotFoundException());
            disputes = contractDisputeRepository.findMyContractDispute(disputeStatus.name(), admin.getId());
        }

        List<ContractDisputeResponse.BasicView> res = new ArrayList<>();
        for(int i = 0; i <  disputes.size(); i++) {

            String openedBy;
            if(disputes.get(i).getDisputeOpenedBy().equals(disputes.get(i).getClientId())) {
                openedBy = "Opened By Client";
            } else openedBy = "Opened By Lawyer";

            res.add(
                    ContractDisputeResponse.BasicView.builder()
                            .disputeId(disputes.get(i).getId())
                            .contractId(disputes.get(i).getContractId())
                            .disputeStatus(disputes.get(i).getDisputeStatus())
                            .gigTitle(disputes.get(i).getTitle())
                            .price(disputes.get(i).getPrice())
                            .openedBy(openedBy)
                            .build()
            );
        }

        return res;
    }

    @Override
    public ContractDisputeResponse.ExtendedView getDisputesExtendedView(UUID disputeId) {
        return null;
    }
}
