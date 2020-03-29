package com.example.chatapp.Model;

public class GroupChat {
    private String sender, messageId, type;
    private String message;
    private String imageUrl;
    private boolean isseen;

    public GroupChat() {
    }

    public GroupChat(String sender, String messageId, String type, String message, String imageUrl, boolean isseen) {
        this.sender = sender;
        this.messageId = messageId;
        this.type = type;
        this.message = message;
        this.imageUrl = imageUrl;
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
