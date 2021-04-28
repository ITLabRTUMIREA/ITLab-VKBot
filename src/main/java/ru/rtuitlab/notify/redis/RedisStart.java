//package ru.rtuitlab.notify.redis;
//
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//
//@Component
//public class RedisStart {
//
//    private final RedisListener redisListener;
//
//    public RedisStart(RedisListener redisListener) {
//        this.redisListener = redisListener;
//    }
//
//    @PostConstruct
//    public void start() {
//        redisListener.listenEvents();
//    }
//}
