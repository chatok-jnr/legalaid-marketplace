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
import com.legal_marketplace.legal_marketplace.service.ContractDeliveryFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractDeliveryFileServiceImpl implements ContractDeliveryFileService {

    private final ContractDeliveryFileRepository contractDeliveryFileRepository;
    private final ContractDeliveryRepository contractDeliveryRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ContractDeliveryFileResponse.Create createContractDeliveryFile(ContractDeliveryFileRequest.Create request, UUID deliveryId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        ContractDelivery delivery = contractDeliveryRepository.findById(deliveryId)
                .orElseThrow(ContractDeliveryExceptions.NotFound::new);

        // Ensure the authenticated user is the one who delivered (delivered_by)
        if(!user.getId().equals(delivery.getDeliveredBy())) {
            throw new UserExceptions.AccessDeniedException();
        }

        ContractDeliveryFile file = ContractDeliveryFile.builder()
                .delivery(delivery)
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .fileSize(request.getFileSize())
                .mimeType(request.getMimeType())
                .uploadedBy(user)
                .createdAt(Instant.now())
                .build();

        ContractDeliveryFile saved = contractDeliveryFileRepository.save(file);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ContractDeliveryFileResponse.Create uploadAndRegisterFile(MultipartFile file, UUID deliveryId, String userEmail) throws IOException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        ContractDelivery delivery = contractDeliveryRepository.findById(deliveryId)
                .orElseThrow(ContractDeliveryExceptions.NotFound::new);

        // Ensure the authenticated user is the one who delivered (delivered_by)
        if(!user.getId().equals(delivery.getDeliveredBy())) {
            throw new UserExceptions.AccessDeniedException();
        }

        // Upload file to Cloudinary
        Map<String, String> uploadResult = cloudinaryService.uploadFile(file, "legalAid/contract-delivery-files");

        // Extract URL and file details
        String fileUrl = uploadResult.get("url");
        String originalFileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // Create and save the file record
        ContractDeliveryFile deliveryFile = ContractDeliveryFile.builder()
                .delivery(delivery)
                .fileName(originalFileName)
                .fileUrl(fileUrl)
                .fileSize((int) fileSize)
                .mimeType(mimeType)
                .uploadedBy(user)
                .createdAt(Instant.now())
                .build();

        ContractDeliveryFile saved = contractDeliveryFileRepository.save(deliveryFile);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteContractDeliveryFile(UUID deliveryId, UUID fileId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(UserExceptions.UserNotFoundException::new);

        ContractDeliveryFile file = contractDeliveryFileRepository.findByIdAndDelivery_Id(fileId, deliveryId)
                .orElseThrow(ContractDeliveryExceptions.NotFound::new);

        // Check if the user is the one who uploaded the file
        if (!user.getId().equals(file.getUploadedBy().getId())) {
            throw new UserExceptions.AccessDeniedException();
        }

        contractDeliveryFileRepository.delete(file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDeliveryFileResponse.Create> getFilesByDeliveryId(UUID deliveryId) {
        List<ContractDeliveryFile> files = contractDeliveryFileRepository.findByDelivery_Id(deliveryId);
        if (files.isEmpty()) throw new ContractDeliveryExceptions.NotFound();

        List<ContractDeliveryFileResponse.Create> res = new ArrayList<>();
        for (ContractDeliveryFile f : files) res.add(toResponse(f));
        return res;
    }

    private ContractDeliveryFileResponse.Create toResponse(ContractDeliveryFile f) {
        ContractDeliveryFileResponse.Create r = new ContractDeliveryFileResponse.Create();
        r.setId(f.getId());
        r.setDeliveryId(f.getDelivery().getId());
        r.setFileName(f.getFileName());
        r.setFileUrl(f.getFileUrl());
        r.setFileSize(f.getFileSize());
        r.setMimeType(f.getMimeType());
        r.setUploadedBy(f.getUploadedBy().getId());
        r.setCreatedAt(f.getCreatedAt());
        return r;
    }
}
