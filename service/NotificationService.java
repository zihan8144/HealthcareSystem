package com.hms.service;

public class NotificationService {
    // simulates SMS or Email gateway
    public void sendNotification(String recipientId, String message) {
        System.out.println("[EXTERNAL NOTIFICATION SERVICE] SMS/Email to " + recipientId + ": " + message);
    }
}