package com.ayshriv.salescrm.company.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.company.dto.CompanyCreateRequest;
import com.ayshriv.salescrm.company.dto.CompanySearchRequest;
import com.ayshriv.salescrm.company.dto.CompanyUpdateRequest;
import com.ayshriv.salescrm.company.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listCompanies(@ModelAttribute CompanySearchRequest request) {
        ApiStatus status = companyService.listCompanies(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "companies", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewCompany(@PathVariable Long id) {
        ApiStatus status = companyService.viewCompany(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "company"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        ApiStatus status = companyService.createCompany(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "company"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editCompany(@PathVariable Long id, @RequestBody CompanyUpdateRequest request) {
        ApiStatus status = companyService.editCompany(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "company"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteCompany(@PathVariable Long id) {
        ApiStatus status = companyService.deleteCompany(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}
