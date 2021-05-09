package ru.rtuitlab.notify.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import ru.rtuitlab.notify.models.Invite;
import ru.rtuitlab.notify.models.Message;
import ru.rtuitlab.notify.models.MessageDTO;
import ru.rtuitlab.notify.redis.RedisPublisher;
import ru.rtuitlab.notify.repositories.InviteRepo;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", matchIfMissing = true)
public class SchedulerService {

    private final RedisPublisher redisPublisher;
    private final InviteRepo inviteRepo;
    private final ObjectMapper om;

    public SchedulerService(RedisPublisher redisPublisher, InviteRepo inviteRepo, ObjectMapper om) {
        this.redisPublisher = redisPublisher;
        this.inviteRepo = inviteRepo;
        this.om = om;
    }

    @Value("${scheduling.message:Вы забыли подтвердить участие в мероприятии}")
    private String remindMessage;
    @Value("${database.redis.sendChannel}")
    private String channel;
    @Value("${secrets.token}")
    private String token;
    @Value("${secrets.url}")
    private String url;

//    @Scheduled(cron = "*/15 * * * * *") // test case
//    @Scheduled(cron = "0 0 14 * * *")
    @PostConstruct //test case
    public void sendReminders() {
        try {
            List<Invite> invites = getInvites();
            if (invites == null) {
                log.info("Today no reminds");
            } else {
                invites = invites.stream().distinct().collect(Collectors.toList());
                MessageDTO messageDTO = makeMessage(invites);
                redisPublisher.publish(channel, om.writeValueAsString(messageDTO));
                log.info("Send reminds");
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public List<Invite> getInvites() {
        return inviteRepo.findAll().stream().distinct().collect(Collectors.toList());
    }

    public MessageDTO makeMessage(List<Invite> invites) {
        if (invites == null) {
            return null;
        }
        Message message = new Message();
        Invite inviteTmp = invites.get(0);
        message.setDate(inviteTmp.getDate());
        message.setTitle(inviteTmp.getEvent());
        message.setBody(remindMessage);

        List<String> users = new ArrayList<>();
        invites.forEach(invite -> {
            users.add(invite.getInvitedId());
        });

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMessage(message);
        messageDTO.setUsers(users);
        return messageDTO;
    }

}
