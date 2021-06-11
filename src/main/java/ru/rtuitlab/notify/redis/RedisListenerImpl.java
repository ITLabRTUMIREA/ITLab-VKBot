package ru.rtuitlab.notify.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ru.rtuitlab.notify.services.MessageHandler;

@Slf4j
@Service
public class RedisListenerImpl implements RedisListener {

    private Jedis jedis;

    @Override
    public void unsubscribe() {
        jedis.close();
        jedis = null;
        log.info("Redis subscribe closed");
    }

    @Override
    public void listenEvents(String host, Integer port,
                             String password, String channel,
                             Integer timeout, MessageHandler messageHandler) {
        if (host != null && port != null && password != null && channel != null) {
            jedis = new Jedis(host, port, timeout);
            String status = jedis.auth(password);
            if (status.equals("OK")) {
                log.info("Connected to redis host");
            } else {
                log.error("Can't connect to redis");
            }
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    log.debug("Channel $channel has sent a message: $message");
                    if (!message.isEmpty()) {
                        messageHandler.handleMessage(message);
                    }
                }

                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    log.info("Subscribed to : " + channel);
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    log.info("Unsubscribed from : " + channel);
                }
            }, channel);

        }
        else {
            log.error("Check redis configuration (host, port, password, channel");
        }
    }
}
