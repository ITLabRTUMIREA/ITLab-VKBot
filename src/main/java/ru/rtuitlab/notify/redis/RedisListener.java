package ru.rtuitlab.notify.redis;

import org.springframework.scheduling.annotation.Async;
import ru.rtuitlab.notify.services.MessageHandler;

public interface RedisListener {

    public void unsubscribe();
    public void listenEvents(String host, Integer port,
                             String password, String channel,
                             Integer timeout, MessageHandler messageHandler);

}
