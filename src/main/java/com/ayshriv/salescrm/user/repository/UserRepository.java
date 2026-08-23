package com.ayshriv.salescrm.user.repository;

import com.ayshriv.salescrm.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    Optional<User> findByEmailVerificationTokenAndIsDeletedFalse(String token);

    Optional<User> findByPasswordResetTokenAndIsDeletedFalse(String token);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsDeletedFalse(String email);

    List<User> findByOrganizationIdAndIsDeletedFalse(Long organizationId);

    Page<User> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<User> findByIsDeletedFalse(Pageable pageable);
}
