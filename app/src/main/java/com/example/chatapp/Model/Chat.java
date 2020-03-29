package com.example.chatapp.Model;

public class Chat {
    private String sender;
    private String receiver;
    private String message, messageId;
    private String type, time, date;
    private boolean isseen;

    public Chat() {

    }

    public Chat(String sender, String receiver, String message, String type, String time, String date, boolean isseen, String messageId) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageId = messageId;
        this.type = type;
        this.time = time;
        this.date = date;
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
