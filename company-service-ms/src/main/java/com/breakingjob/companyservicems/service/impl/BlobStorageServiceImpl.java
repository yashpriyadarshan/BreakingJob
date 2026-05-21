package com.breakingjob.companyservicems.service.impl;


import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.breakingjob.companyservicems.exception.FileStorageException;
import com.breakingjob.companyservicems.service.BlobStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
public class BlobStorageServiceImpl implements BlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Override
    public String uploadFile(MultipartFile file, String containerName) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        try {

            BlobServiceClient blobServiceClient =
                    new BlobServiceClientBuilder()
                            .connectionString(connectionString)
                            .buildClient();

            BlobContainerClient containerClient =
                    blobServiceClient.getBlobContainerClient(containerName);

            if (!containerClient.exists()) {
                throw new FileStorageException(
                        "Container does not exist: " + containerName
                );
            }

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                throw new FileStorageException("Invalid file name");
            }

            String fileName =
                    UUID.randomUUID() + originalFileName.substring(originalFileName.lastIndexOf('.'));

            BlobClient blobClient =
                    containerClient.getBlobClient(fileName);

            blobClient.upload(
                    file.getInputStream(),
                    file.getSize(),
                    true
            );

            String uploadedFileUrl = blobClient.getBlobUrl();

            log.info("File uploaded successfully: {}", uploadedFileUrl);

            return uploadedFileUrl;

        } catch (BlobStorageException e) {

            log.error(
                    "Azure Blob Storage error while uploading file",
                    e
            );

            throw new FileStorageException(
                    "Cloud storage error occurred while uploading file"
            );

        } catch (IOException e) {

            log.error(
                    "Failed to read file input stream",
                    e
            );

            throw new FileStorageException(
                    "Failed to process uploaded file"
            );

        } catch (IllegalArgumentException e) {

            log.error(
                    "Invalid blob storage configuration",
                    e
            );

            throw new FileStorageException(
                    "Invalid storage configuration"
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error occurred while uploading file",
                    e
            );

            throw new FileStorageException(
                    "Unexpected error occurred while uploading file"
            );
        }
    }

    @Override
    public void deleteFile(String fileUrl) {

        if (fileUrl == null || fileUrl.isBlank()) {
            throw new FileStorageException("File URL is invalid");
        }

        try {

            BlobClient blobClient =
                    new BlobClientBuilder()
                            .endpoint(fileUrl)
                            .connectionString(connectionString)
                            .buildClient();

            if (!blobClient.exists()) {

                log.warn(
                        "File not found in blob storage: {}",
                        fileUrl
                );

                return;
            }

            blobClient.delete();

            log.info(
                    "File deleted successfully: {}",
                    fileUrl
            );

        } catch (BlobStorageException e) {

            log.error(
                    "Azure Blob Storage error while deleting file: {}",
                    fileUrl,
                    e
            );

            throw new FileStorageException(
                    "Cloud storage error occurred while deleting file"
            );

        } catch (IllegalArgumentException e) {

            log.error(
                    "Invalid blob URL or configuration: {}",
                    fileUrl,
                    e
            );

            throw new FileStorageException(
                    "Invalid file URL"
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error occurred while deleting file: {}",
                    fileUrl,
                    e
            );

            throw new FileStorageException(
                    "Unexpected error occurred while deleting file"
            );
        }
    }

    // To generate public URL, Valid only for ${expiryMinutes} minute
    @Override
    public String generateSignedUrl (
            String fileUrl,
            int expiryMinutes
    ) {
        log.debug(fileUrl);
        BlobClient blobClient =
                new BlobClientBuilder()
                        .endpoint(fileUrl)
                        .connectionString(connectionString)
                        .buildClient();

        BlobSasPermission permission =
                new BlobSasPermission()
                        .setReadPermission(true);

        OffsetDateTime expiryTime =
                OffsetDateTime.now()
                        .plusMinutes(expiryMinutes);

        BlobServiceSasSignatureValues values =
                new BlobServiceSasSignatureValues(
                        expiryTime,
                        permission
                );

        String sasToken =
                blobClient.generateSas(values);

        return fileUrl + "?" + sasToken;
    }
}