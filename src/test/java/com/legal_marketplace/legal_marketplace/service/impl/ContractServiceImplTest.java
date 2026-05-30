package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.response.ContractResponse;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.entity.enums.ContractStatus;
import com.legal_marketplace.legal_marketplace.entity.enums.DisputeStatus;
import com.legal_marketplace.legal_marketplace.entity.enums.PaymentStatus;
import com.legal_marketplace.legal_marketplace.repository.ContractRepository;
import com.legal_marketplace.legal_marketplace.repository.GigRepository;
import com.legal_marketplace.legal_marketplace.repository.PaymentRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.repository.projections.ContractExtendedViewProjection;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContractServiceImplTest {

    @Test
    void getContractById_mapsDisputeFieldsFromProjection() {
        UUID contractId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID lawyerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID disputeId = UUID.randomUUID();
        UUID disputeAdminId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2025-01-02T00:00:00Z");
        Instant paidAt = Instant.parse("2025-01-03T00:00:00Z");
        Instant escrowReleasedAt = Instant.parse("2025-01-04T00:00:00Z");
        Instant disputeOpenedAt = Instant.parse("2025-01-05T00:00:00Z");
        Instant disputeResolvedAt = Instant.parse("2025-01-06T00:00:00Z");

        User client = new User();
        client.setId(clientId);
        client.setEmail("client@example.com");

        Map<String, Object> values = new HashMap<>();
        values.put("getClientName", "Client One");
        values.put("getLawyerName", "Lawyer One");
        values.put("getGigTitle", "Corporate advisory");
        values.put("getClientId", clientId);
        values.put("getLawyerId", lawyerId);
        values.put("getPriceAtHire", 2500);
        values.put("getRequirements", "Need review of agreement");
        values.put("getRevisionLeft", 2);
        values.put("getStatus", "accepted");
        values.put("getCancellationReason", null);
        values.put("getCancelledAt", null);
        values.put("getCancelledBy", null);
        values.put("getDeliveryDeadline", createdAt.plusSeconds(86_400));
        values.put("getCreatedAt", createdAt);
        values.put("getUpdatedAt", updatedAt);
        values.put("getPaymentID", paymentId);
        values.put("getPlatformFeeAmount", 250);
        values.put("getLawyerPayoutAmount", 2250);
        values.put("getPaymentStatus", PaymentStatus.HELD);
        values.put("getPaymentReference", "pay_ref_123");
        values.put("getPaidAt", paidAt);
        values.put("getEscrowReleasedAt", escrowReleasedAt);
        values.put("getDisputeId", disputeId);
        values.put("getDisputeStatus", DisputeStatus.UNDER_REVIEW);
        values.put("getDisputeReason", "Delivery delay");
        values.put("getDisputeOpenedBy", clientId);
        values.put("getDisputeAdminId", disputeAdminId);
        values.put("getDisputeOpenedAt", disputeOpenedAt);
        values.put("getDisputeResolvedAt", disputeResolvedAt);
        values.put("getDisputeResolution", "Resolved in favor of client");

        ContractExtendedViewProjection projection = projection(values);

        ContractRepository contractRepository = proxy(ContractRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findContractById")) {
                return projection;
            }
            return defaultValue(method.getReturnType());
        });

        UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByEmail")) {
                return Optional.of(client);
            }
            return defaultValue(method.getReturnType());
        });

        GigRepository gigRepository = proxy(GigRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
        PaymentRepository paymentRepository = proxy(PaymentRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        ContractServiceImpl service = new ContractServiceImpl(contractRepository, gigRepository, userRepository, paymentRepository);

        ContractResponse.ContractExtendedView response = service.getContractById(contractId, client.getEmail(), false);

        assertEquals(clientId, response.getClientId());
        assertEquals(lawyerId, response.getLawyerId());
        assertEquals("Client One", response.getClientName());
        assertEquals("Lawyer One", response.getLawyerName());
        assertEquals("Corporate advisory", response.getGigTitle());
        assertEquals(ContractStatus.ACCEPTED, response.getStatus());
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(PaymentStatus.HELD, response.getPaymentStatus());
        assertEquals(disputeId, response.getDisputeId());
        assertEquals(DisputeStatus.UNDER_REVIEW, response.getDisputeStatus());
        assertEquals("Delivery delay", response.getDisputeReason());
        assertEquals(clientId, response.getDisputeOpenedBy());
        assertEquals(disputeAdminId, response.getDisputeAdminId());
        assertEquals(disputeOpenedAt, response.getDisputeOpenedAt());
        assertEquals(disputeResolvedAt, response.getDisputeResolvedAt());
        assertEquals("Resolved in favor of client", response.getDisputeResolution());
        assertNull(response.getGigId());
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            if (returnType == Optional.class) {
                return Optional.empty();
            }
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }

    private static ContractExtendedViewProjection projection(Map<String, Object> values) {
        return proxy(ContractExtendedViewProjection.class, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "ContractExtendedViewProjectionStub";
                    case "hashCode" -> values.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                };
            }

            if (values.containsKey(method.getName())) {
                return values.get(method.getName());
            }
            return defaultValue(method.getReturnType());
        });
    }
}

