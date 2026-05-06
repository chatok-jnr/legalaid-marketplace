package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDeliveryFileRequest;
import com.legal_marketplace.legal_marketplace.dto.request.ContractDeliveryRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryFileResponse;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ContractDeliveryFileService {
    ContractDeliveryFileResponse.Create createContractDeliveryFile(ContractDeliveryFileRequest.Create request, UUID deliveryId, String userEmail);
    ContractDeliveryFileResponse.Create uploadAndRegisterFile(MultipartFile file, UUID deliveryId, String userEmail) throws IOException;
    void deleteContractDeliveryFile(UUID deliveryId, UUID fileId, String userEmail);
    List<ContractDeliveryFileResponse.Create> getFilesByDeliveryId(UUID deliveryId);
}
