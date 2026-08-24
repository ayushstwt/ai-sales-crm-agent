package com.ayshriv.salescrm.company.repository;

import com.ayshriv.salescrm.company.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByIdAndIsDeletedFalse(Long id);

    Optional<Company> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<Company> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<Company> findByIsDeletedFalse(Pageable pageable);

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIsDeletedFalse(Long organizationId, String name);
}
