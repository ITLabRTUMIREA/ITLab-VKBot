package ru.rtuitlab.notify.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtuitlab.notify.services.CommentService;
import ru.rtuitlab.notify.services.EventService;
import ru.rtuitlab.notify.services.MessageHandler;
import ru.rtuitlab.notify.services.ReportService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RedisStarter {

//    private final RedisListener redisListener;
    private final ReportService reportService;
    private final EventService eventService;
    private final CommentService commentService;

//    private final ReportService reportService;

    @Value("${database.redis.host:localhost}")
    private String host;
    @Value("${database.redis.port:6379}")
    private Integer port;
    @Value("${database.redis.password:}")
    private String password;
    @Value("#{'${database.redis.channels}'.split(',')}")
    private List<String> channels;
    @Value("${database.redis.timeout:30}")
    private Integer timeout;

    public RedisStarter(ReportService reportService, EventService eventService, CommentService commentService) {
        this.reportService = reportService;
        this.eventService = eventService;
        this.commentService = commentService;
    }


    @PostConstruct
    private void init() {
        initListeners(channels);
    }

    private void initListeners(List<String> channels) {
        ExecutorService executorService = Executors.newFixedThreadPool(channels.size());
        executorService.execute(new Listener(reportService, channels.get(0)));
        executorService.execute(new Listener(eventService, channels.get(1)));
        executorService.execute(new Listener(commentService, channels.get(2)));
    }


    private class Listener implements Runnable {

        private final MessageHandler messageHandler;
        private final String channel;

        private Listener(MessageHandler messageHandler, String channel) {
            this.messageHandler = messageHandler;
            this.channel = channel;
        }

        @Override
        public void run() {
            RedisListener redisListener = new RedisListenerImpl();
            redisListener.listenEvents(host, port, password, channel, timeout, messageHandler);
        }
    }
}
