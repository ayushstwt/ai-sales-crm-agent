package com.ayshriv.salescrm.ai.repository;

import com.ayshriv.salescrm.ai.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findByConversationIdAndOrganizationIdAndIsDeletedFalseOrderByCreatedOnAsc(Long conversationId, Long organizationId);

    List<ConversationMessage> findByConversationIdAndIsDeletedFalseOrderByCreatedOnAsc(Long conversationId);
}
