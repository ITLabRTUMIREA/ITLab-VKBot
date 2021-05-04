package ru.rtuitlab.notify.services;

public interface MessageHandler {
    public void handleMessage(String message);
    public void sendMessage(String message);
}
