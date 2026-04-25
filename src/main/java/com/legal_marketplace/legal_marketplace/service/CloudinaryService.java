package com.legal_marketplace.legal_marketplace.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;


    // Create file
    public Map<String, String> uploadFile(MultipartFile file, String folderName) throws IOException {
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", folderName, // Optional: organize uploads into folders
                "resource_type", "auto" // Automatically detect file type
        );

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        Map<String, String> response =  new HashMap<>();
        response.put("url", uploadResult.get("secure_url").toString());
        response.put("publicId", uploadResult.get("public_id").toString());
        return response;
    }

    // Delete a file from Cloudinary
    public Map<?, ?> deleteFile(String publicId, String resourceType) throws IOException {
        Map<String, Object> deleteParams = ObjectUtils.asMap(
                "invalidate", true,  // This invalidates cached copies
                "resource_type", resourceType  // "image", "video", or "raw"
        );

        Map<?, ?> result = cloudinary.uploader().destroy(publicId, deleteParams);
        return result;
    }
}
