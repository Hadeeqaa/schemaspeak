package com.schemaspeak.schemaspeak;

public class InterpretResponse {
    private String receivedSentence;
    private String status;

    public InterpretResponse(String receivedSentence, String status) {
        this.receivedSentence = receivedSentence;
        this.status = status;
    }

    public String getReceivedSentence() { return receivedSentence; }
    public String getStatus() { return status; }
}