package com.ayshriv.salescrm.user.repository;

import com.ayshriv.salescrm.user.entity.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    Page<UserLog> findByIsDeletedFalse(Pageable pageable);

    Page<UserLog> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<UserLog> findByIdAndIsDeletedFalse(Long id);

    List<UserLog> findByUserIdAndIsDeletedFalseOrderByCreatedOnDesc(Long userId);
}
