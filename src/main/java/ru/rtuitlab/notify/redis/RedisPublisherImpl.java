package ru.rtuitlab.notify.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class RedisPublisherImpl implements RedisPublisher{

    private Jedis jedis;

    @Value("${database.redis.host:localhost}")
    private String host;
    @Value("${database.redis.port:6379}")
    private Integer port;
    @Value("${database.redis.password:}")
    private String password;
    @Value("${database.redis.channel:}")
    private String channel;

    public void unsubscribe() {
        jedis.close();
        jedis = null;
        log.info("Redis subscribe closed");
    }

    @PostConstruct
    private void init() {
        connect(host, port, password);
    }


    @Override
    public void connect(String host, Integer port,
                        String password) {
        if (host != null && port != null && password != null) {
            jedis = new Jedis(host, port);
            String status = jedis.auth(password);
            if (status.equals("OK")) {
                log.info("Connected to redis host");
            } else {
                log.error("Can't connect to redis");
            }
        }
    }

    @Override
    public void publish(String channel, String message) {
        jedis.publish(channel, message);
    }
}
