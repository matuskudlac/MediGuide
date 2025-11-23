package com.team.mediguide.ui.ai;

import androidx.lifecycle.ViewModel;
import com.google.ai.client.generativeai.type.Content;
import com.team.mediguide.ChatMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for AiFragment
 * Persists chat messages and conversation history across fragment lifecycle
 */
public class AiViewModel extends ViewModel {
    
    private List<ChatMessage> chatMessages;
    private List<Content> chatHistory;
    
    public AiViewModel() {
        chatMessages = new ArrayList<>();
        chatHistory = new ArrayList<>();
    }
    
    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }
    
    public List<Content> getChatHistory() {
        return chatHistory;
    }
    
    public void clearConversation() {
        chatMessages.clear();
        chatHistory.clear();
    }
}
