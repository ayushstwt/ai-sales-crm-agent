package com.ayshriv.salescrm.ai.service;

import com.ayshriv.salescrm.ai.service.impl.OpenAIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpenAIProviderTest {

    @Mock
    private ChatModel chatModel;

    private OpenAIProvider openAIProvider;

    @BeforeEach
    void setUp() {
        openAIProvider = new OpenAIProvider(chatModel);
    }

    @Test
    @DisplayName("generateText(String) calls chatModel.call(String)")
    void testGenerateTextString() {
        when(chatModel.call("Hello AI")).thenReturn("Hello Human");

        String response = openAIProvider.generateText("Hello AI");

        assertThat(response).isEqualTo("Hello Human");
        verify(chatModel).call("Hello AI");
    }

    @Test
    @DisplayName("generateText(List<Message>) calls chatModel.call(Prompt) and unwraps result")
    void testGenerateTextMessageList() {
        Generation generation = new Generation("Response from prompt");
        ChatResponse chatResponse = new ChatResponse(List.of(generation));

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        String result = openAIProvider.generateText(List.of(new UserMessage("Hi")));

        assertThat(result).isEqualTo("Response from prompt");
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    @DisplayName("generateText with system and user prompts builds messages correctly")
    void testGenerateTextSystemAndUser() {
        Generation generation = new Generation("System-guided reply");
        ChatResponse chatResponse = new ChatResponse(List.of(generation));

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        String result = openAIProvider.generateText("You are an assistant.", "Hello!");

        assertThat(result).isEqualTo("System-guided reply");
    }
}