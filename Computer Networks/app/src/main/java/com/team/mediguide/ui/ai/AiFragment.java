package com.team.mediguide.ui.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ServerException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.team.mediguide.BuildConfig;
import com.team.mediguide.ChatMessage;
import com.team.mediguide.ChatMessageAdapter;
import com.team.mediguide.R;

import java.util.ArrayList;
import java.util.List;

public class AiFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> chatMessages; // For UI display
    private List<Content> chatHistory; // For conversation history
    private EditText chatInput;
    private Button sendButton;
    private GenerativeModelFutures generativeModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ai, container, false);

        chatRecyclerView = root.findViewById(R.id.chat_recyclerview);
        chatInput = root.findViewById(R.id.chat_input);
        sendButton = root.findViewById(R.id.send_button);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.MEDI_GUIDE_API_KEY);
        generativeModel = GenerativeModelFutures.from(gm);

        // Initialize chat history with the system instruction
        chatHistory = new ArrayList<>();
        Content systemInstruction = new Content.Builder()
                .addText("You are MediGuide's friendly and helpful AI assistant. Your goal is to help users with questions about their health and the products we sell. Keep your answers concise and clear.")
                .build();
        chatHistory.add(systemInstruction);

        sendButton.setOnClickListener(v -> {
            String messageText = chatInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });

        return root;
    }

    private void sendMessage(String messageText) {
        // Add user message to UI and history
        chatMessages.add(new ChatMessage(messageText, true));
        chatHistory.add(new Content.Builder().addText(messageText).build());
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatInput.setText("");

        // Add loading indicator
        chatMessages.add(new ChatMessage("...", false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        // Convert the history to the required array format
        Content[] historyArray = chatHistory.toArray(new Content[0]);

        // Send the entire history to the model
        ListenableFuture<GenerateContentResponse> response = generativeModel.generateContent(historyArray);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                // Add model response to history and update UI
                chatHistory.add(result.getCandidates().get(0).getContent());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        chatMessages.remove(chatMessages.size() - 1);
                        chatMessages.add(new ChatMessage(resultText, false));
                        chatAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        chatMessages.remove(chatMessages.size() - 1);
                        if (t instanceof ServerException) {
                            chatMessages.add(new ChatMessage("Sorry, the AI is currently overloaded. Please try again later.", false));
                        } else {
                            chatMessages.add(new ChatMessage("An error occurred. Please try again.", false));
                        }
                        chatAdapter.notifyDataSetChanged();
                    });
                }
                t.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }
}