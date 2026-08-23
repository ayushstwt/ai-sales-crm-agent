package com.ayshriv.salescrm.organization.repository;

import com.ayshriv.salescrm.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByNameAndIsDeletedFalse(String name);

    Optional<Organization> findBySlugAndIsDeletedFalse(String slug);

    Optional<Organization> findByIdAndIsDeletedFalse(Long id);

    boolean existsByNameAndIsDeletedFalse(String name);

    boolean existsBySlugAndIsDeletedFalse(String slug);
}
