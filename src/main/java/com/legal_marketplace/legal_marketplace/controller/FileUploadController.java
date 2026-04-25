package com.legal_marketplace.legal_marketplace.controller;

import com.legal_marketplace.legal_marketplace.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    private final CloudinaryService cloudinaryService;

    @PreAuthorize("hasAnyRole('LAWYER')")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file")
            MultipartFile file
    ) {
        try{
            if(file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            //String fileUrl = cloudinaryService.uploadFile(file, "legalAid/lawyer-gigs-media");

            Map<String, String> response = cloudinaryService.uploadFile(file, "legalAid/lawyer-gigs-media");
           // response.put("url", fileUrl);
            return ResponseEntity.ok(response);
        } catch(IOException e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('LAWYER')")
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(
            @RequestParam("publicId") String publicId,
            @RequestParam(value = "resourceType", defaultValue = "image") String resourceType) {

        try {
            Map<?, ?> result = cloudinaryService.deleteFile(publicId, resourceType);

            System.out.println(publicId);

            Map<String, String> response = new HashMap<>();

            // Check if deletion was successful
            if ("ok".equals(result.get("result"))) {
                response.put("message", "File deleted successfully");
                response.put("publicId", publicId);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to delete file: " + result.get("result"));
                return ResponseEntity.status(404).body(response);
            }

        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Delete failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
