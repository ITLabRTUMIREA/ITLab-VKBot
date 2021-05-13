package ru.rtuitlab.notify.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtuitlab.notify.models.Message;
import ru.rtuitlab.notify.models.MessageDTO;
import ru.rtuitlab.notify.models.Report;
import ru.rtuitlab.notify.models.User;
import ru.rtuitlab.notify.redis.RedisPublisher;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class ReportService implements MessageHandler{

    private final RedisPublisher redisPublisher;
    private final UserService userService;
    private final ObjectMapper om;

    public ReportService(RedisPublisher redisPublisher, UserService userService, ObjectMapper om) {
        this.redisPublisher = redisPublisher;
        this.userService = userService;
        this.om = om;
    }

    @Value("${database.redis.sendChannel}")
    private String channel;

    /**
     * Handle messages in comments channel in redis
     * @param message - String, should be json object of Report entity
     */
    @Override
    public void handleMessage(String message) {
        log.info("Report service handle message: " + message);
        sendMessage(message);
    }

    /**
     * Method which send notification about report if another user wrote about him
     * @param message - Json object of Report Entity
     */
    @Override
    public void sendMessage(String message) {
        try {
            Report report = om.readValue(message, Report.class);
            if (report.getSenderId().equals(report.getReceiverId())) {
                log.info("User " + report.getSenderId() + " wrote report about him/herself");
                return;
            }
            Optional<User> user = userService.getUser(report.getSenderId());
            if (!user.isPresent()) {
                log.info("user " + report.getSenderId() + " not found");
                return;
            }
            String sender = user.get().getLastName() + ' ' + user.get().getFirstName();
            MessageDTO messageDTO = makeMessage(report, sender);
            redisPublisher.publish(channel, om.writeValueAsString(messageDTO));
            log.info("report publish: " + messageDTO);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public MessageDTO makeMessage(Report report, String user) {
        Message message = new Message();
        message.setTitle("О вас написали отчет");
        message.setBody(
                String.format("Пользователь %s написал о вас отчет",
                        user));

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUsers(Collections.singletonList(report.getReceiverId()));
        messageDTO.setMessage(message);
        return messageDTO;
    }
}
