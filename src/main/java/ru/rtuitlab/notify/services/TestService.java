package ru.rtuitlab.notify.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService implements MessageHandler{
    @Override
    public void handleMessage(String message) {
        log.info("TEST SERVICE === " + message);
    }

    @Override
    public void sendMessage(String message) {

    }
}
