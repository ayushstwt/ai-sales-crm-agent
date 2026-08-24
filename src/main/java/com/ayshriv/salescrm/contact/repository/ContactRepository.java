package com.ayshriv.salescrm.contact.repository;

import com.ayshriv.salescrm.contact.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByIdAndIsDeletedFalse(Long id);

    Optional<Contact> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<Contact> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<Contact> findByIsDeletedFalse(Pageable pageable);

    List<Contact> findByCompanyIdAndIsDeletedFalse(Long companyId);
}
