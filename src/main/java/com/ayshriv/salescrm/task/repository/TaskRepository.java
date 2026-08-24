package com.ayshriv.salescrm.task.repository;

import com.ayshriv.salescrm.task.entity.Task;
import com.ayshriv.salescrm.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndIsDeletedFalse(Long id);

    Optional<Task> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Page<Task> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<Task> findByOrganizationIdAndStatusAndIsDeletedFalse(Long organizationId, TaskStatus status, Pageable pageable);

    Page<Task> findByOrganizationIdAndAssignedToIdAndIsDeletedFalse(Long organizationId, Long assignedToId, Pageable pageable);

    List<Task> findByOrganizationIdAndRelatedTypeAndRelatedIdAndIsDeletedFalse(Long organizationId, String relatedType, Long relatedId);

    Page<Task> findByIsDeletedFalse(Pageable pageable);
}