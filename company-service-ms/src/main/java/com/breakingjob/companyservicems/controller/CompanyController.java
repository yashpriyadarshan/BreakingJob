    package com.breakingjob.companyservicems.controller;

    import com.breakingjob.companyservicems.dto.request.CreateCompanyRequest;
    import com.breakingjob.companyservicems.dto.request.UpdateCompanyRequest;
    import com.breakingjob.companyservicems.dto.response.CompanyResponse;
    import com.breakingjob.companyservicems.exception.FileStorageException;
    import com.breakingjob.companyservicems.service.CompanyService;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    @Slf4j
    @RestController
    @RequestMapping("/v1/company")
    @RequiredArgsConstructor
    public class CompanyController {

        private final CompanyService companyService;

        /* ──────────────────────────────────────────────
         *  Company CRUD Endpoints
         * ──────────────────────────────────────────────
         */
        @PostMapping
        public ResponseEntity<CompanyResponse> createCompany(
                @RequestBody CreateCompanyRequest request) {

            log.info("Creating company/recruiter profile with id: {}", request.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(companyService.createCompany(request));
        }

        @PutMapping
        public ResponseEntity<CompanyResponse> updateCompany(
                @RequestBody UpdateCompanyRequest request) {
            log.info("Updating company profile with id: {}", request.getId());
            return ResponseEntity.ok(companyService.updateCompany(request));
        }

        @GetMapping("/{id}")
        public ResponseEntity<CompanyResponse> getCompany(@PathVariable Long id) {
            log.info("Fetching company profile with id: {}", id);
            return ResponseEntity.ok(companyService.getCompany(id));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<CompanyResponse> deleteCompany(
                @PathVariable Long id,
                @RequestHeader("Authorization") String token
                ) {
            log.info("Deleting company profile with id: {}", id);
            companyService.deleteCompany(id, token);
            return ResponseEntity.noContent().build();
        }

        @GetMapping
        public ResponseEntity<CompanyResponse> getCompany(HttpServletRequest request) {
            return ResponseEntity.ok(companyService.getCompany(request));
        }

        @PostMapping("/{id}/logo")
        public ResponseEntity<CompanyResponse> uploadLogo(
                @PathVariable Long id,
                @Valid @RequestParam("file") MultipartFile file) {

            if(file.isEmpty()) {
                throw new FileStorageException("File cannot be empty");
            }

            return ResponseEntity.ok(
                    companyService.uploadLogo(id, file)
            );
        }
    }
