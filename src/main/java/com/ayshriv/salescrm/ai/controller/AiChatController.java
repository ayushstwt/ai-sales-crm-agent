package com.ayshriv.salescrm.ai.controller;

import com.ayshriv.salescrm.ai.dto.ChatMessageDto;
import com.ayshriv.salescrm.ai.dto.ChatRequest;
import com.ayshriv.salescrm.ai.dto.ChatResponse;
import com.ayshriv.salescrm.ai.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * Throwaway endpoint to test raw completion from LLMProvider per Step 5.1.
     */
    @GetMapping("/completion")
    public ResponseEntity<Map<String, String>> getRawCompletion(
            @RequestParam(defaultValue = "Hello! Tell me in one sentence what you are.") String prompt
    ) {
        String completion = aiChatService.getRawCompletion(prompt);
        return ResponseEntity.ok(Map.of("prompt", prompt, "completion", completion));
    }

    /**
     * Throwaway POST endpoint for raw completion with custom body.
     */
    @PostMapping("/completion")
    public ResponseEntity<Map<String, String>> postRawCompletion(@RequestBody Map<String, String> body) {
        String prompt = body.getOrDefault("prompt", "Hello");
        String completion = aiChatService.getRawCompletion(prompt);
        return ResponseEntity.ok(Map.of("prompt", prompt, "completion", completion));
    }

    /**
     * AI chat endpoint per Step 5.2. Returns dedicated ChatResponse DTO (not ApiStatus).
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiChatService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Conversation history endpoint.
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getConversationMessages(@PathVariable Long conversationId) {
        List<ChatMessageDto> messages = aiChatService.getConversationMessages(conversationId);
        return ResponseEntity.ok(messages);
    }
}
