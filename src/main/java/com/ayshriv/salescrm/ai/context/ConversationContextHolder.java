package com.ayshriv.salescrm.ai.context;

/**
 * ThreadLocal holder for the current active conversation ID during AI chat request execution.
 */
public final class ConversationContextHolder {

    private static final ThreadLocal<Long> CURRENT_CONVERSATION_ID = new ThreadLocal<>();

    private ConversationContextHolder() {
    }

    public static void setConversationId(Long conversationId) {
        CURRENT_CONVERSATION_ID.set(conversationId);
    }

    public static Long getConversationId() {
        return CURRENT_CONVERSATION_ID.get();
    }

    public static void clear() {
        CURRENT_CONVERSATION_ID.remove();
    }
}
