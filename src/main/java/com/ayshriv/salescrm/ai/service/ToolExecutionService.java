package com.ayshriv.salescrm.ai.service;

import com.ayshriv.salescrm.ai.entity.ToolExecution;

import java.util.List;

public interface ToolExecutionService {

    ToolExecution recordExecution(String toolName, String arguments, String result, String status, Long executionTimeMs);

    ToolExecution recordExecution(Long conversationId, String toolName, String arguments, String result, String status, Long executionTimeMs);

    List<ToolExecution> getExecutionsForConversation(Long conversationId);
}