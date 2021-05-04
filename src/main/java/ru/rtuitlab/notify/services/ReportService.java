package ru.rtuitlab.notify.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtuitlab.notify.models.Message;
import ru.rtuitlab.notify.models.MessageDTO;
import ru.rtuitlab.notify.models.Report;
import ru.rtuitlab.notify.redis.RedisPublisher;

import java.util.Collections;

@Slf4j
@Service
public class ReportService implements MessageHandler{

    private final RedisPublisher redisPublisher;
    private final ObjectMapper om;

    public ReportService(RedisPublisher redisPublisher, ObjectMapper om) {
        this.redisPublisher = redisPublisher;
        this.om = om;
    }

    @Value("${database.redis.sendChannel}")
    private String channel;

    @Override
    public void handleMessage(String message) {
        System.out.println("ReportService here! + " + message);
        sendMessage(message);
    }

    @Override
    public void sendMessage(String message) {
        try {
            Report report = om.readValue(message, Report.class);
            MessageDTO messageDTO = makeMessage(report);
            redisPublisher.publish(channel, om.writeValueAsString(messageDTO));
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public MessageDTO makeMessage(Report report) {
        Message message = new Message();
        // FIXME Change title
        message.setTitle("На вас написан донос");
        message.setDate(report.getDate());
        message.setBody(
                String.format("Пользователь %s написал о вас отчет",
                        report.getSender()));

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUsers(Collections.singletonList(report.getReceiverId()));
        messageDTO.setMessage(message);
        return messageDTO;
    }
}
