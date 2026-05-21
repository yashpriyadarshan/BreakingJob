package com.breakingjob.userservicems.service;

import org.springframework.web.multipart.MultipartFile;

public interface BlobStorageService {
    String uploadFile(MultipartFile file, String containerName);

    void deleteFile(String fileUrl);

    String generateSignedUrl(String fileUrl, int expiryMinutes);
}
