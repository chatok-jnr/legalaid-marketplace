package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.ContractRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractResponse;
import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    ContractResponse.Create createContract(ContractRequest.Crete request, String clientEmail); // Only Client
    ContractResponse.ContractExtendedView cancelContract(ContractRequest.Cancel request, UUID contractId,String userEmail);
    List<ContractResponse.ContractView> getMyContracts(String userEmail, String role);
    ContractResponse.ContractExtendedView getContractById(UUID contractId, String userEmail);

    // Accept and Reject methods for Lawyer
    ContractStatus acceptContract(UUID contractId, String lawyerEmail);
    ContractStatus rejectContract(UUID contractId, String lawyerEmail);

    // Update to In Progress
    ContractStatus contractInProgress(UUID contractId, String userEmail);

    // Complete The contract by Client
    ContractStatus completeContract(UUID contractId, String clientEmail);
}
