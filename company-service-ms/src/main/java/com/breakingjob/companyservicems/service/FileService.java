package com.breakingjob.companyservicems.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String saveFile(MultipartFile file);

    void delete(String filename);
}
