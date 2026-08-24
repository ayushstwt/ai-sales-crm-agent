package com.ayshriv.salescrm.ai.repository;

import com.ayshriv.salescrm.ai.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    List<Conversation> findByOrganizationIdAndUserIdAndIsDeletedFalseOrderByCreatedOnDesc(Long organizationId, Long userId);

    Page<Conversation> findByOrganizationIdAndUserIdAndIsDeletedFalse(Long organizationId, Long userId, Pageable pageable);

    Optional<Conversation> findByIdAndIsDeletedFalse(Long id);
}
