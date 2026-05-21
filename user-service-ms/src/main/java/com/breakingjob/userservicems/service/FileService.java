package com.breakingjob.userservicems.service;

import com.breakingjob.userservicems.enums.FileType;
import com.breakingjob.userservicems.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    String saveFile(MultipartFile file, FileType type);

    void delete(String resume);
}
