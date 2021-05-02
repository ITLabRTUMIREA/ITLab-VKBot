package ru.rtuitlab.notify.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class RedisListenerImpl implements RedisListener {

    private Jedis jedis;

    @Value("${database.redis.host:localhost}")
    private String host;
    @Value("${database.redis.port:6379}")
    private Integer port;
    @Value("${database.redis.password:}")
    private String password;
    @Value("${database.redis.channel:}")
    private String channel;
    @Value("${database.redis.timeout:30}")
    private Integer timeout;

    @PostConstruct
    public void start(){
        listenEvents();
    }

    @Override
    public void unsubscribe() {
        jedis.close();
        jedis = null;
        log.info("Redis subscribe closed");
    }

    @Override
    public void listenEvents() {
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
                        String res = new GsonBuilder()
                                .setPrettyPrinting()
                                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                .create()
                                .fromJson(message, String.class);
                        System.out.println("!!!!! " + res);
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
