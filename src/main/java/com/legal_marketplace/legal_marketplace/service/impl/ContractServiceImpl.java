package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.ContractRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractResponse;
import com.legal_marketplace.legal_marketplace.entity.Contract;
import com.legal_marketplace.legal_marketplace.entity.ContractPayment;
import com.legal_marketplace.legal_marketplace.entity.Gig;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
import com.legal_marketplace.legal_marketplace.exception.ContractExceptions;
import com.legal_marketplace.legal_marketplace.exception.GigExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.ContractRepository;
import com.legal_marketplace.legal_marketplace.repository.GigRepository;
import com.legal_marketplace.legal_marketplace.repository.PaymentRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.repository.projectiions.ContractExtendedViewProjection;
import com.legal_marketplace.legal_marketplace.repository.projectiions.ContractViewProjection;
import com.legal_marketplace.legal_marketplace.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final GigRepository gigRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override // Create Contract
    @Transactional
    public ContractResponse.Create createContract(ContractRequest.Crete request, String clientEmail) {

        Gig gig = gigRepository.findById(request.getGigId())
                .orElseThrow(GigExceptions.GigNotFoundException::new);

        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        // Request DTO -> Entity
        Contract contract = contractRepository.save(Contract.builder()
                .clientId(user.getId())
                .lawyerId(gig.getLawyerId())
                .gig(gig)
                .maxRevision(gig.getMaxRevision())
                .priceAtHire(gig.getMinPrice())
                .requirements(request.getRequirements())
                .revisionsLeft(gig.getMaxRevision())
                .status(ContractStatus.PENDING)
                .deliveryDeadline(request.getDeliveryDeadline())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        paymentRepository.save(ContractPayment.builder()
                        .contract(contract)
                        .platformFeePercent(BigDecimal.valueOf(10.00)) // Assuming 10% platform fee percentage
                        .platformFeeAmount((int) (gig.getMinPrice() * 0.10)) // Assuming 10% platform fee
                        .lawyerPayoutAmount((int) (gig.getMinPrice() * 0.90)) // Assuming lawyer gets 90%
                        .paymentStatus(com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus.UNPAID)
                .build());

        // Entity to Response DTO
        return ContractResponse.Create.builder()
                .id(contract.getId())
                .clientId(user.getId())
                .lawyerId(gig.getLawyerId())
                .gigId(request.getGigId())
                .priceAtHire(gig.getMinPrice())
                .requirements(request.getRequirements())
                .revisionsLeft(gig.getMaxRevision())
                .status(contract.getStatus())
                .deliveryDeadline(contract.getDeliveryDeadline())
                .createdAt(contract.getCreatedAt())
                .build();
    }

    @Override // Cancell Contract
    public ContractResponse.ContractExtendedView cancelContract(ContractRequest.Cancel request, UUID contractId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(ContractExceptions.NotFound::new);

        if(!user.getId().equals(contract.getClientId()) && !user.getId().equals(contract.getLawyerId())){
            throw new UserExceptions.AccessDeniedException();
        }

        if(contract.getStatus().equals(ContractStatus.PENDING) || contract.getStatus().equals(ContractStatus.ACCEPTED) || contract.getStatus().equals(ContractStatus.IN_PROGRESS)){
            contract.setStatus(ContractStatus.CANCELLED);
            contract.setCancelledBy(user.getId());
            contract.setCancelledAt(Instant.now());
            contract.setCancellationReason(request.getCancelledReason());
            contractRepository.save(contract);

            return entityToExtendedView(contract);
        } else {
            throw new ContractExceptions.BadRequest("Contract cannot be cancelled as it is already " + contract.getStatus());
        }
    }

    @Override // Get a user all my contracts basic info
    public List<ContractResponse.ContractView> getMyContracts(String userEmail, String role) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

         if(role.equalsIgnoreCase("client")){
             List<ContractViewProjection> contractViewProjection = contractRepository.findClientContractsByUserId(user.getId());
             List<ContractResponse.ContractView> contractViews = new ArrayList<>();
             for(ContractViewProjection cvp: contractViewProjection){
                 contractViews.add(entityToContractView(cvp));
             }
             return contractViews;
         } else if(role.equalsIgnoreCase("lawyer")){
             List<ContractViewProjection> contractViewProjection = contractRepository.findLawyerContractsByUserId(user.getId());
             List<ContractResponse.ContractView> contractViews = new ArrayList<>();
             for(ContractViewProjection cvp: contractViewProjection){
                 contractViews.add(entityToContractView(cvp));
             }
             return contractViews;
         } else {
             throw new ContractExceptions.BadRequest("Role have to be either client or lawyer");
         }
    }

    @Override // Get contract Extended view by id
    public ContractResponse.ContractExtendedView getContractById(UUID contractId, String userEmail) {
        ContractExtendedViewProjection cev = contractRepository.findContractById(contractId);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        System.out.println(userEmail);

        if(!user.getId().equals(cev.getClientId()) && !user.getId().equals(cev.getLawyerId())){
            throw new UserExceptions.AccessDeniedException();
        }

        return ContractResponse.ContractExtendedView.builder()
                .id(cev.getId())
                .clientId(cev.getClientId())
                .lawyerId(cev.getLawyerId())
                .clientName(cev.getClientName())
                .lawyerName(cev.getLawyerName())
                .gigId(cev.getGigId())
                .gigTitle(cev.getGigTitle())
                .priceAtHire(cev.getPriceAtHire())
                .requirements(cev.getRequirements())
                .revisionsLeft(cev.getRevisionLeft())
                .status(ContractStatus.valueOf(cev.getStatus().trim().toUpperCase()))
                .cancellationReason(cev.getCancellationReason())
                .cancelledAt(cev.getCancelledAt())
                .cancelledBy(cev.getCancelledBy())
                .deliveryDeadline(cev.getDeliveryDeadline())
                .createdAt(cev.getCreatedAt())
                .updatedAt(cev.getUpdatedAt())

                // Payment Info
                .paymentId(cev.getPaymentID())
                .platformFeeAmount(cev.getPlatformFeeAmount())
                .lawyerPayoutAmount(cev.getLawyerPayoutAmount())
                .paymentStatus(cev.getPaymentStatus())
                .paymentReference(cev.getPaymentReference())
                .paidAt(cev.getPaidAt())
                .escrowReleasedAt(cev.getEscrowReleasedAt())

                // Dispute Info
                .disputeId(cev.getDisputeId())
                .disputeStatus(cev.getDisputeStatus())
                .disputeReason(cev.getDisputeReason())
                .disputeOpenedBy(cev.getDisputeOpenedBy())
                .disputeAdminId(cev.getDisputeAdminId())
                .disputeOpenedAt(cev.getDisputeOpenedAt())
                .disputeResolvedAt(cev.getDisputeResolvedAt())
                .disputeResolution(cev.getDisputeResolution())
                .build();
    }

    // Accept Contract
    @Override
    public ContractStatus acceptContract(UUID contractId, String lawyerEmail) {
        User user = userRepository.findByEmail(lawyerEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(ContractExceptions.NotFound::new);

        if(!user.getId().equals(contract.getLawyerId())){
            throw new UserExceptions.AccessDeniedException();
        }

        if(!contract.getStatus().equals(ContractStatus.PENDING)){
            throw new ContractExceptions.BadRequest("Only pending contracts can be accepted");
        }

        contract.setStatus(ContractStatus.ACCEPTED);
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);
        return contract.getStatus();
    }

    // Reject Contract
    @Override
    public ContractStatus rejectContract(UUID contractId, String lawyerEmail) {
        User user = userRepository.findByEmail(lawyerEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(ContractExceptions.NotFound::new);

        if(!user.getId().equals(contract.getLawyerId())){
            throw new UserExceptions.AccessDeniedException();
        }

        if(!contract.getStatus().equals(ContractStatus.PENDING)){
            throw new ContractExceptions.BadRequest("Only pending contracts can be rejected");
        }

        contract.setStatus(ContractStatus.CANCELLED);
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);
        return contract.getStatus();
    }

    // In progre
    @Override
    public ContractStatus contractInProgress(UUID contractId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(ContractExceptions.NotFound::new);
        if(!user.getId().equals(contract.getLawyerId()) && !user.getId().equals(contract.getClientId())){
            throw new UserExceptions.AccessDeniedException();
        }

        if(!contract.getStatus().equals(ContractStatus.ACCEPTED) && !contract.getStatus().equals(ContractStatus.DELIVERED)
            && !contract.getStatus().equals(ContractStatus.IN_REVISION)
        ){
            throw new ContractExceptions.BadRequest("Only Accepted, Delivered or In Revision contracts can be marked as In Progress");
        }
        contract.setStatus(ContractStatus.IN_PROGRESS);
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);
        return contract.getStatus();
    }

    // Complete Contract By Client
    @Override
    public ContractStatus completeContract(UUID contractId, String clientEmail) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(ContractExceptions.NotFound::new);
        if(!user.getId().equals(contract.getClientId())){
            throw new UserExceptions.AccessDeniedException();
        }

        if(!contract.getStatus().equals(ContractStatus.IN_PROGRESS)
                && !contract.getStatus().equals(ContractStatus.DELIVERED)
                && !contract.getStatus().equals(ContractStatus.IN_REVISION)
        ){
            throw new ContractExceptions.BadRequest("Only In Progress, Delivered or In Revision contracts can be marked as Completed");
        }

        contract.setStatus(ContractStatus.COMPLETED);
        contract.setUpdatedAt(Instant.now());
        contractRepository.save(contract);
        return ContractStatus.COMPLETED;
    }

    // ============================
    // Functions for internal use (not exposed in service interface)
    // ============================
    private ContractResponse.ContractExtendedView entityToExtendedView(Contract contract){
        return ContractResponse.ContractExtendedView.builder()
                .id(contract.getId())
                .clientId(contract.getClientId())
                .lawyerId(contract.getLawyerId())
                .gigId(contract.getGig().getId())
                .priceAtHire(contract.getPriceAtHire())
                .requirements(contract.getRequirements())
                .revisionsLeft(contract.getRevisionsLeft())
                .status(contract.getStatus())
                .deliveryDeadline(contract.getDeliveryDeadline())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .cancelledAt(contract.getCancelledAt())
                .cancellationReason(contract.getCancellationReason())
                .cancelledBy(contract.getCancelledBy())
                .build();
    }

    private ContractResponse.ContractView entityToContractView(ContractViewProjection request){
        return ContractResponse.ContractView.builder()
                .id(request.getId())
                .gigTitle(request.getGigTitle())
                .clientName(request.getClientName())
                .lawyerName(request.getLawyerName())
                .priceAtHire(request.getPriceAtHire())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
