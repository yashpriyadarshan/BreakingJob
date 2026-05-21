package com.breakingjob.companyservicems.service.impl;

import com.breakingjob.companyservicems.exception.FileStorageException;
import com.breakingjob.companyservicems.service.BlobStorageService;
import com.breakingjob.companyservicems.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final BlobStorageService blobStorageService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> IMAGE_EXTENSIONS =
            Set.of(".png", ".jpg", ".jpeg", ".gif");

    private static final Set<String> DOCUMENT_EXTENSIONS =
            Set.of(".pdf", ".docx");

    @Override
    public String saveFile(MultipartFile file) {

        validateFile(file);

        String containerName = "logo-container";

        try {

            String uploadedFileUrl =
                    blobStorageService.uploadFile(
                            file,
                            containerName
                    );

            log.info(
                    "File uploaded successfully for type {}: {}",
                    "logo",
                    uploadedFileUrl
            );

            return uploadedFileUrl;

        } catch (FileStorageException e) {

            log.error(
                    "File storage error while uploading file",
                    e
            );

            throw e;

        } catch (Exception e) {

            log.error(
                    "Unexpected error while uploading file",
                    e
            );

            throw new FileStorageException(
                    "Failed to upload file"
            );
        }
    }

    @Override
    public void delete(String fileUrl) {

        if (fileUrl == null || fileUrl.isBlank()) {
            throw new FileStorageException(
                    "File URL is invalid"
            );
        }

        try {

            blobStorageService.deleteFile(fileUrl);

            log.info(
                    "Deleted file from blob storage: {}",
                    fileUrl
            );

        } catch (FileStorageException e) {

            log.error(
                    "File storage error while deleting file: {}",
                    fileUrl,
                    e
            );

            throw e;

        } catch (Exception e) {

            log.error(
                    "Unexpected error while deleting file: {}",
                    fileUrl,
                    e
            );

            throw new FileStorageException(
                    "Failed to delete file"
            );
        }
    }

    private void validateFile(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException(
                    "File is empty"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException(
                    "File size exceeds limit (5MB)"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new FileStorageException(
                    "Invalid file name"
            );
        }

        if (!fileName.contains(".")) {
            throw new FileStorageException(
                    "File extension is missing"
            );
        }

        String extension =
                getFileExtension(fileName).toLowerCase();

        log.debug(
                "Validating file: {} with extension: {}",
                fileName,
                extension
        );

        if (!IMAGE_EXTENSIONS.contains(extension)) {

            log.warn(
                    "Invalid image extension received: {}",
                    extension
            );

            throw new FileStorageException(
                    "Invalid image file type"
            );
        }
    }

    private String getFileExtension(String fileName) {

        int lastDotIndex = fileName.lastIndexOf(".");

        if (lastDotIndex == -1) {
            throw new FileStorageException(
                    "File extension not found"
            );
        }

        return fileName.substring(lastDotIndex);
    }
}