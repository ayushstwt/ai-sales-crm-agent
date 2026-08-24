package com.ayshriv.salescrm.ai.repository;

import com.ayshriv.salescrm.ai.entity.ToolExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolExecutionRepository extends JpaRepository<ToolExecution, Long> {

    List<ToolExecution> findByConversationIdAndIsDeletedFalseOrderByCreatedOnAsc(Long conversationId);

    List<ToolExecution> findByOrganizationIdAndIsDeletedFalseOrderByCreatedOnDesc(Long organizationId);

    List<ToolExecution> findByToolNameAndIsDeletedFalseOrderByCreatedOnDesc(String toolName);
}