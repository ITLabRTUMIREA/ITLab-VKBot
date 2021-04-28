package ru.rtuitlab.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.rtuitlab.notify.redis.RedisStart;

@SpringBootApplication
public class Notify {

    public static void main(String[] args) {
        SpringApplication.run(Notify.class, args);
    }

}
