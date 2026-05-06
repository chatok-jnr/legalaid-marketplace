package com.legal_marketplace.legal_marketplace.service.impl;

import com.legal_marketplace.legal_marketplace.dto.request.ContractDeliveryFileRequest;
import com.legal_marketplace.legal_marketplace.dto.response.ContractDeliveryFileResponse;
import com.legal_marketplace.legal_marketplace.entity.ContractDelivery;
import com.legal_marketplace.legal_marketplace.entity.ContractDeliveryFile;
import com.legal_marketplace.legal_marketplace.entity.User;
import com.legal_marketplace.legal_marketplace.exception.ContractDeliveryExceptions;
import com.legal_marketplace.legal_marketplace.exception.UserExceptions;
import com.legal_marketplace.legal_marketplace.repository.ContractDeliveryFileRepository;
import com.legal_marketplace.legal_marketplace.repository.ContractDeliveryRepository;
import com.legal_marketplace.legal_marketplace.repository.UserRepository;
import com.legal_marketplace.legal_marketplace.service.CloudinaryService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractDeliveryFileServiceImplTest {

    @Test
    void createContractDeliveryFile_savesAndReturnsResponse_whenUserOwnsDelivery() {
        UUID deliveryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("lawyer@example.com");

        ContractDelivery delivery = new ContractDelivery();
        delivery.setId(deliveryId);
        delivery.setDeliveredBy(userId);

        ContractDeliveryFileRequest.Create request = new ContractDeliveryFileRequest.Create();
        request.setFileName("agreement.pdf");
        request.setFileUrl("https://cdn.example.com/agreement.pdf");
        request.setFileSize(2048);
        request.setMimeType("application/pdf");

        final ContractDeliveryFile[] saved = new ContractDeliveryFile[1];

        UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByEmail")) {
                return Optional.of(user);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryRepository deliveryRepository = proxy(ContractDeliveryRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findById")) {
                return Optional.of(delivery);
            }
            if (method.getName().equals("countByContractId")) {
                return 0L;
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryFileRepository fileRepository = proxy(ContractDeliveryFileRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("save")) {
                saved[0] = (ContractDeliveryFile) args[0];
                saved[0].setId(UUID.randomUUID());
                return saved[0];
            }
            if (method.getName().equals("findByDelivery_Id")) {
                return List.of(saved[0]);
            }
            return defaultValue(method.getReturnType());
        });

        CloudinaryService cloudinaryService = null; // Not needed for this test

        ContractDeliveryFileServiceImpl service = new ContractDeliveryFileServiceImpl(
                fileRepository,
                deliveryRepository,
                userRepository,
                cloudinaryService
        );

        ContractDeliveryFileResponse.Create response = service.createContractDeliveryFile(request, deliveryId, user.getEmail());

        assertEquals(deliveryId, response.getDeliveryId());
        assertEquals(userId, response.getUploadedBy());
        assertEquals("agreement.pdf", response.getFileName());
        assertEquals("https://cdn.example.com/agreement.pdf", response.getFileUrl());
        assertEquals(2048, response.getFileSize());
        assertEquals("application/pdf", response.getMimeType());
        assertEquals(saved[0].getId(), response.getId());
    }

    @Test
    void createContractDeliveryFile_throwsAccessDenied_whenUserDidNotDeliver() {
        UUID deliveryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("lawyer@example.com");

        ContractDelivery delivery = new ContractDelivery();
        delivery.setId(deliveryId);
        delivery.setDeliveredBy(UUID.randomUUID());

        ContractDeliveryFileRequest.Create request = new ContractDeliveryFileRequest.Create();
        request.setFileName("agreement.pdf");
        request.setFileUrl("https://cdn.example.com/agreement.pdf");
        request.setFileSize(2048);
        request.setMimeType("application/pdf");

        UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByEmail")) {
                return Optional.of(user);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryRepository deliveryRepository = proxy(ContractDeliveryRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findById")) {
                return Optional.of(delivery);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryFileRepository fileRepository = proxy(ContractDeliveryFileRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        CloudinaryService cloudinaryService = null; // Not needed for this test

        ContractDeliveryFileServiceImpl service = new ContractDeliveryFileServiceImpl(
                fileRepository,
                deliveryRepository,
                userRepository,
                cloudinaryService
        );

        assertThrows(UserExceptions.AccessDeniedException.class,
                () -> service.createContractDeliveryFile(request, deliveryId, user.getEmail()));
    }

    @Test
    void deleteContractDeliveryFile_deletesWhenUserUploadedFile() {
        UUID deliveryId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("lawyer@example.com");

        ContractDelivery delivery = new ContractDelivery();
        delivery.setId(deliveryId);

        ContractDeliveryFile file = new ContractDeliveryFile();
        file.setId(fileId);
        file.setDelivery(delivery);
        file.setUploadedBy(user);

        final boolean[] deleted = {false};

        UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByEmail")) {
                return Optional.of(user);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryRepository deliveryRepository = proxy(ContractDeliveryRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        ContractDeliveryFileRepository fileRepository = proxy(ContractDeliveryFileRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByIdAndDelivery_Id")) {
                return Optional.of(file);
            }
            if (method.getName().equals("delete")) {
                deleted[0] = true;
                return null;
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryFileServiceImpl service = new ContractDeliveryFileServiceImpl(
                fileRepository,
                deliveryRepository,
                userRepository,
                null
        );

        service.deleteContractDeliveryFile(deliveryId, fileId, user.getEmail());

        assertTrue(deleted[0]);
    }

    @Test
    void deleteContractDeliveryFile_throwsAccessDenied_whenDifferentUserDeletesFile() {
        UUID deliveryId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User uploader = new User();
        uploader.setId(UUID.randomUUID());
        uploader.setEmail("uploader@example.com");

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");

        ContractDelivery delivery = new ContractDelivery();
        delivery.setId(deliveryId);

        ContractDeliveryFile file = new ContractDeliveryFile();
        file.setId(fileId);
        file.setDelivery(delivery);
        file.setUploadedBy(uploader);

        UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByEmail")) {
                return Optional.of(otherUser);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryRepository deliveryRepository = proxy(ContractDeliveryRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        ContractDeliveryFileRepository fileRepository = proxy(ContractDeliveryFileRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByIdAndDelivery_Id")) {
                return Optional.of(file);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryFileServiceImpl service = new ContractDeliveryFileServiceImpl(
                fileRepository,
                deliveryRepository,
                userRepository,
                null
        );

        assertThrows(UserExceptions.AccessDeniedException.class,
                () -> service.deleteContractDeliveryFile(deliveryId, fileId, otherUser.getEmail()));
    }

    @Test
    void deleteContractDeliveryFile_throwsNotFound_whenFileDoesNotExistForDelivery() {
        UUID deliveryId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("lawyer@example.com");

        UserRepository userRepository = proxy(UserRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByEmail")) {
                return Optional.of(user);
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryRepository deliveryRepository = proxy(ContractDeliveryRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

        ContractDeliveryFileRepository fileRepository = proxy(ContractDeliveryFileRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("findByIdAndDelivery_Id")) {
                return Optional.empty();
            }
            return defaultValue(method.getReturnType());
        });

        ContractDeliveryFileServiceImpl service = new ContractDeliveryFileServiceImpl(
                fileRepository,
                deliveryRepository,
                userRepository,
                null
        );

        assertThrows(ContractDeliveryExceptions.NotFound.class,
                () -> service.deleteContractDeliveryFile(deliveryId, fileId, user.getEmail()));
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
