package com.breakingjob.companyservicems.service.impl;

import com.breakingjob.companyservicems.dto.request.CreateCompanyRequest;
import com.breakingjob.companyservicems.dto.request.UpdateCompanyRequest;
import com.breakingjob.companyservicems.dto.response.CompanyResponse;
import com.breakingjob.companyservicems.entity.Company;
import com.breakingjob.companyservicems.exception.ResourceNotFoundException;
import com.breakingjob.companyservicems.exception.ServiceCommunicationException;
import com.breakingjob.companyservicems.jwt.JwtUtils;
import com.breakingjob.companyservicems.mapper.CompanyMapper;
import com.breakingjob.companyservicems.repository.CompanyRepository;
import com.breakingjob.companyservicems.service.BlobStorageService;
import com.breakingjob.companyservicems.service.CompanyService;
import com.breakingjob.companyservicems.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final JwtUtils jwtUtils;
    private final FileService fileService;
    private final BlobStorageService blobStorageService;

    private final RestTemplate restTemplate;
    /**
     *   Private Helpers
     */

    private Company findCompanyOrThrow(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));
    }

    private CompanyResponse buildReponseWithSignedUrl(Company user) {
        log.info("converting url to public");

        CompanyResponse response = companyMapper.toResponse(user);

        if(user.getLogoUrl() != null) {
            response.setLogoUrl(
                    blobStorageService.generateSignedUrl(user.getLogoUrl(), 30)
            );
        }

        return response;
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        log.info("Creating company with id: {}", request.getId());

        Company company = companyMapper.toEntity(request);
        companyRepository.save(company);

        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional
    public @Nullable CompanyResponse getCompany(Long companyId) {
        log.debug("Fetching company by id: {}", companyId);

        Company company = findCompanyOrThrow(companyId);
        return buildReponseWithSignedUrl(company);
    }

    @Override
    public @Nullable CompanyResponse getCompany(HttpServletRequest request) {
        String jwtToken = jwtUtils.getJwtFromHeader(request);
        Long companyId = jwtUtils.getUserIdFromToken(jwtToken);

        log.debug("Fetching company for authenticated company id: {}", companyId);

        Company company = findCompanyOrThrow(companyId);
        return buildReponseWithSignedUrl(company);
    }

    @Override
    @Transactional
    public @Nullable CompanyResponse updateCompany(UpdateCompanyRequest request) {
        log.debug("Updating user profile with id: {}", request.getId());

        Company company = findCompanyOrThrow(request.getId());

        companyMapper.toEntity(company, request);
        companyRepository.save(company);

        return buildReponseWithSignedUrl(company);
    }

    @Override
    @Transactional
    public void deleteCompany(Long id, String token) {
        log.info("Initiating deletion for company id {}", id);

        Company company = findCompanyOrThrow(id);
        String logoUrl = company.getLogoUrl();

        deleteFromAuthService(id, token);

        companyRepository.delete(company);
        log.info("Company deleted from database for id: {}", id);

        deleteFileSilently(logoUrl);
    }

    @Override
    @Transactional
    public CompanyResponse uploadLogo(Long companyId, MultipartFile file) {
        Company company = findCompanyOrThrow(companyId);
        String url = fileService.saveFile(file);
        company.setLogoUrl(url);

        CompanyResponse response = companyMapper.toResponse(company);
        response.setLogoUrl(blobStorageService.generateSignedUrl(url, 30));

        return response;
    }

    private void deleteFileSilently(String fileUrl) {
        if (fileUrl == null) return;

        try {
            fileService.delete(fileUrl);
        } catch (Exception e) {
            log.warn("Failed to delete company logo from storage: {}", e.getMessage());
        }
    }

    private void deleteFromAuthService(Long userId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.startsWith("Bearer ") ? token.substring(7) : token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    authServiceUrl + "/api/v1/auth",
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceCommunicationException(
                        "Auth Service",
                        "Deletion rejected with status: " + response.getStatusCode()
                );
            }

            log.info("Auth profile deleted successfully for user id: {}", userId);

        } catch (RestClientException e) {
            log.error("Failed to contact Auth service for user id: {}", userId, e);
            throw new ServiceCommunicationException("Auth Service", "Service unreachable", e);
        }
    }
}
