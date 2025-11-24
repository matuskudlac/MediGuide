package com.team.mediguide;

public class ChatMessage {
    private String text;
    private boolean fromUser;

    public ChatMessage(String text, boolean fromUser) {
        this.text = text;
        this.fromUser = fromUser;
    }

    public String getText() {
        return text;
    }

    public boolean isFromUser() {
        return fromUser;
    }
}
