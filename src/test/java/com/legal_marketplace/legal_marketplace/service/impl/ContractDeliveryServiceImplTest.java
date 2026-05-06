package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryResponse;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.repository.ContractDeliveryRepository;
import com.legal_marketplace.legal_marketplace.repository.ContractRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.repository.projections.ContractDeliveryWithFiles;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractDeliveryServiceImplTest {

    @Test
    void getContractDeliveryByDeliveryId_mapsProjectionAndFilesIntoResponse() {
        UUID deliveryId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID lawyerId = UUID.randomUUID();

        User user = new User();
        user.setId(clientId);
        user.setEmail("client@example.com");

        Instant deliveredAt = Instant.parse("2025-01-01T10:15:30Z");
        Instant revisionRequestedAt = Instant.parse("2025-01-02T10:15:30Z");
        Instant completedAt = Instant.parse("2025-01-03T10:15:30Z");

        String deliveryFilesJson = """
                [
                  {
                    "fileName": "draft-contract.pdf",
                    "url": "https://cdn.example.com/draft-contract.pdf",
                    "fileSize": 4096,
                    "mimeType": "application/pdf"
                  },
                  {
                    "fileName": "cover-letter.docx",
                    "url": "https://cdn.example.com/cover-letter.docx",
                    "fileSize": 1024,
                    "mimeType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                  }
                ]
                """;

        ContractDeliveryWithFiles projection = proxy(ContractDeliveryWithFiles.class, (proxy, method, args) -> switch (method.getName()) {
            case "getDeliveryId" -> deliveryId;
            case "getClientId" -> clientId;
            case "getLawyerId" -> lawyerId;
            case "getContractId" -> contractId;
            case "getDeliveryNote" -> "Please review the attached draft";
            case "getDeliveredAt" -> deliveredAt;
            case "getRevisionRequestedAt" -> revisionRequestedAt;
            case "getRevisionNote" -> "Please revise clause 3";
            case "getCompletedAt" -> completedAt;
            case "getDeliveryFiles" -> deliveryFilesJson;
            default -> defaultValue(method.getReturnType());
        });

        UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByEmail")) {
                return Optional.of(user);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryRepository deliveryRepository = proxy(ContractDeliveryRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findWithFilesByDeliveryId")) {
                return projection;
            }
            return defaultValue(method.getReturnType());
        });

        ContractRepository contractRepository = proxy(ContractRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        ContractDeliveryServiceImpl service = new ContractDeliveryServiceImpl(
                userRepository,
                deliveryRepository,
                contractRepository,
                new ObjectMapper()
        );

        ContractDeliveryResponse.AllInfo response = service.getContractDeliveryByDeliveryId(deliveryId, user.getEmail());

        assertEquals(deliveryId, response.getDeliveryId());
        assertEquals(clientId, response.getClientId());
        assertEquals(lawyerId, response.getLawyerId());
        assertEquals(contractId, response.getContractId());
        assertEquals("Please review the attached draft", response.getDeliveryNote());
        assertEquals(deliveredAt, response.getDeliveredAt());
        assertEquals(revisionRequestedAt, response.getRevisionRequestedAt());
        assertEquals("Please revise clause 3", response.getRevisionNote());
        assertEquals(completedAt, response.getCompletedAt());
        assertEquals(2, response.getDeliveryFiles().size());
        assertEquals("draft-contract.pdf", response.getDeliveryFiles().getFirst().getFileName());
        assertEquals("https://cdn.example.com/draft-contract.pdf", response.getDeliveryFiles().getFirst().getUrl());
        assertEquals(4096, response.getDeliveryFiles().getFirst().getFileSize());
        assertEquals("application/pdf", response.getDeliveryFiles().getFirst().getMimeType());
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            if (returnType == Optional.class) {
                return Optional.empty();
            }
            if (returnType == List.class) {
                return List.of();
            }
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0F;
        if (returnType == double.class) return 0D;
        if (returnType == char.class) return '\0';
        return null;
    }
}

