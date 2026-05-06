package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDeliveryRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryResponse;
import com.legal_marketplace.legal_marketplace.entity.Contract;
import com.legal_marketplace.legal_marketplace.entity.ContractDelivery;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
import com.legal_marketplace.legal_marketplace.exception.ContractDeliveryExceptions;
import com.legal_marketplace.legal_marketplace.exception.ContractExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.ContractDeliveryRepository;
import com.legal_marketplace.legal_marketplace.repository.ContractRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.repository.projections.ContractDeliveryWithFiles;
import com.legal_marketplace.legal_marketplace.service.ContractDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractDeliveryServiceImpl implements ContractDeliveryService {

    private final UserRepository userRepository;
    private final ContractDeliveryRepository contractDeliveryRepository;
    private final ContractRepository contractRepository;
    private final ObjectMapper objectMapper;

    // Create Delivery
    @Override
    @Transactional
    public ContractDeliveryResponse.Create createContractDelivery(ContractDeliveryRequest.Create request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(ContractExceptions.NotFound::new);

        if(!user.getId().equals(contract.getLawyerId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        ContractDelivery contractDelivery = contractDeliveryRepository.save(
                ContractDelivery.builder()
                        .contract(contract)
                        .deliveryNumber(contractDeliveryRepository.countByContractId(contract.getId()) + 1)
                        .deliveredBy(user.getId())
                        .deliveryNote(request.getDeliveryNote())
                        .deliveredAt(Instant.now())
                .build()
        );

        contract.setStatus(ContractStatus.DELIVERED);
        contractRepository.save(contract);

        return ContractDeliveryResponse.Create.builder()
                .id(contractDelivery.getId())
                .contractId(contractDelivery.getContract().getId())
                .deliveryNumber(contractDelivery.getDeliveryNumber())
                .deliveredBy(contractDelivery.getDeliveredBy())
                .deliveryNote(contractDelivery.getDeliveryNote())
                .deliveredAt(contractDelivery.getDeliveredAt())
                .build();
    }

    // Get Delivery Details(files) by Delivery ID
    @Override
    public ContractDeliveryResponse.AllInfo getContractDeliveryByDeliveryId(UUID deliveryId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        ContractDeliveryWithFiles delivery = contractDeliveryRepository.findWithFilesByDeliveryId(deliveryId);

        if (delivery == null) {
            throw new ContractDeliveryExceptions.NotFound();
        }

        if (!user.getId().equals(delivery.getClientId()) && !user.getId().equals(delivery.getLawyerId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        return ContractDeliveryResponse.AllInfo.builder()
                .deliveryId(delivery.getDeliveryId())
                .clientId(delivery.getClientId())
                .lawyerId(delivery.getLawyerId())
                .contractId(delivery.getContractId())
                .deliveryNote(delivery.getDeliveryNote())
                .deliveredAt(delivery.getDeliveredAt())
                .revisionRequestedAt(delivery.getRevisionRequestedAt())
                .revisionNote(delivery.getRevisionNote())
                .completedAt(delivery.getCompletedAt())
                .deliveryFiles(parseDeliveryFiles(delivery.getDeliveryFiles()))
                .build();
    }


    // Get All List of Deliveries for a Contract by contract ID
    @Override
    public List<ContractDeliveryResponse.Create> getAllContractDeliveriesByContractId(UUID contractId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(ContractExceptions.NotFound::new);
        if(!contract.getLawyerId().equals(user.getId()) && !contract.getClientId().equals(user.getId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        List<ContractDelivery> deliveries = contractDeliveryRepository.findByContractId(contractId);
        List<ContractDeliveryResponse.Create> responseList = new ArrayList<>();
        for(ContractDelivery contractDelivery : deliveries) {
            responseList.add(
                    ContractDeliveryResponse.Create.builder()
                            .id(contractDelivery.getId())
                            .contractId(contractDelivery.getContract().getId())
                            .deliveryNumber(contractDelivery.getDeliveryNumber())
                            .deliveredBy(contractDelivery.getDeliveredBy())
                            .deliveryNote(contractDelivery.getDeliveryNote())
                            .deliveredAt(contractDelivery.getDeliveredAt())
                            .build()
            );
        }

        return  responseList;
    }

    @Override
    @Transactional
    public ContractDeliveryResponse.Revision requestRevision(UUID deliveryId, ContractDeliveryRequest.Revision request, String userEmail) {
         User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);
         ContractDelivery contractDelivery = contractDeliveryRepository.findById(deliveryId)
                 .orElseThrow(ContractDeliveryExceptions.NotFound::new);
         Contract contract = contractRepository.findById(contractDelivery.getContract().getId())
                 .orElseThrow(ContractExceptions.NotFound::new);

         if(!contract.getClientId().equals(user.getId())) {
                throw new UserExceptions.AccessDeniedException();
         }

         if(contract.getRevisionsLeft() <= 0) {
             throw new ContractDeliveryExceptions.NoRevisionLeft();
         }

         if(!contract.getStatus().equals(ContractStatus.DELIVERED)) {
            throw new ContractDeliveryExceptions.BadRequest("Contract is not in DELIVERED status, cannot request revision");
         }

         if(contractDelivery.getRevisionRequestedAt() != null) {
             throw new ContractDeliveryExceptions.BadRequest("Revision has already been requested for this delivery");
         }

         contract.setRevisionsLeft(contract.getRevisionsLeft() - 1);
         contract.setStatus(ContractStatus.IN_REVISION);
         contract.setUpdatedAt(Instant.now());
         contractRepository.save(contract);

         contractDelivery.setRevisionRequestedAt(Instant.now());
         contractDelivery.setRevisionNote(request.getRevisionNote());
         contractDeliveryRepository.save(contractDelivery);

         return ContractDeliveryResponse.Revision.builder()
                 .revisionRequestedAt(contractDelivery.getRevisionRequestedAt())
                 .revisionNote(contractDelivery.getRevisionNote())
                 .build();
    }

    // ==============================================================
    // Functions
    // =-============================================================

    private List<ContractDeliveryResponse.DeliveryFile> parseDeliveryFiles(String deliveryFilesJson) {
        try {
            return (deliveryFilesJson == null || deliveryFilesJson.isBlank())
                    ? List.of()
                    : objectMapper.readValue(deliveryFilesJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error parsing delivery files JSON: {}", e.getMessage());
            return List.of();
        }
    }
}
