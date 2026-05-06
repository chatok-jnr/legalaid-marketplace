package com.legal_marketplace.legal_marketplace.service;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDeliveryRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryResponse;

import java.util.List;
import java.util.UUID;

public interface ContractDeliveryService {
    ContractDeliveryResponse.Create createContractDelivery(ContractDeliveryRequest.Create request, String userEmail);
    ContractDeliveryResponse.AllInfo getContractDeliveryByDeliveryId(UUID deliveryId, String userEmail);
    List<ContractDeliveryResponse.Create> getAllContractDeliveriesByContractId(UUID contractId, String userEmail);
    ContractDeliveryResponse.Revision requestRevision(UUID deliveryId, ContractDeliveryRequest.Revision request, String userEmail);
}
