package ru.rtuitlab.notify.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtuitlab.notify.models.Comment;
import ru.rtuitlab.notify.models.Message;
import ru.rtuitlab.notify.models.MessageDTO;
import ru.rtuitlab.notify.redis.RedisPublisher;

import java.util.Collections;

@Slf4j
@Service
public class CommentService implements MessageHandler {

    private final RedisPublisher redisPublisher;
    private final ObjectMapper om;

    public CommentService(RedisPublisher redisPublisher, ObjectMapper om) {
        this.redisPublisher = redisPublisher;
        this.om = om;
    }

    @Value("${database.redis.sendChannel}")
    private String channel;

    @Override
    public void handleMessage(String message) {
        System.out.println("CommentService here! + " + message);
        sendMessage(message);
    }

    @Override
    public void sendMessage(String message) {
        try {
            Comment comment = om.readValue(message, Comment.class);
            MessageDTO messageDTO = makeMessage(comment);
            redisPublisher.publish(channel, om.writeValueAsString(messageDTO));
            log.info("comment publish: " + messageDTO);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public MessageDTO makeMessage(Comment comment) {
        Message message = new Message();
        message.setTitle("О вас оставили комментарий");
        message.setBody(
                String.format("Пользователь %s оставил комменатрий на ваш отчет '%s'",
                comment.getSender(), comment.getReport()));
        message.setDate(comment.getDate());

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUsers(Collections.singletonList(comment.getUser()));
        messageDTO.setMessage(message);
        return messageDTO;
    }
}
