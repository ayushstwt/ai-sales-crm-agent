package com.ayshriv.salescrm.company.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.company.dto.CompanyCreateRequest;
import com.ayshriv.salescrm.company.dto.CompanySearchRequest;
import com.ayshriv.salescrm.company.dto.CompanyUpdateRequest;

public interface CompanyService {

    ApiStatus listCompanies(CompanySearchRequest request);

    ApiStatus viewCompany(Long id);

    ApiStatus createCompany(CompanyCreateRequest request);

    ApiStatus editCompany(Long id, CompanyUpdateRequest request);

    ApiStatus deleteCompany(Long id);
}
