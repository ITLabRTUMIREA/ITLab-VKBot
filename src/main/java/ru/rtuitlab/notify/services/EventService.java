package ru.rtuitlab.notify.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtuitlab.notify.models.*;
import ru.rtuitlab.notify.redis.RedisPublisher;
import ru.rtuitlab.notify.repositories.InviteRepo;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EventService implements MessageHandler{

    private final RedisPublisher redisPublisher;
    private final InviteRepo inviteRepo;
    private final ObjectMapper om;

    public EventService(RedisPublisher redisPublisher, InviteRepo inviteRepo, ObjectMapper om) {
        this.redisPublisher = redisPublisher;
        this.inviteRepo = inviteRepo;
        this.om = om;
    }

    @Value("${database.redis.sendChannel}")
    private String channel;

    @PostConstruct
    private void init() throws JsonProcessingException {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setDate("01.05.21");
        eventDTO.setText("hello world");
        eventDTO.setPayment("1000");
        eventDTO.setTitle("Delegation");
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(12L);
        list.add(24L);
        eventDTO.setInvitedIds(list);
        String message = om.writeValueAsString(eventDTO);
        log.info("EventPost: " + message);
        handleMessage(message);
    }

    @Override
    public void handleMessage(String message) {
        System.out.println("EventService here! + " + message);
        if (message.substring(0, 6).equals("accept")) {
            receiveAccept(message);
        }
        else {
            sendEvent(message);
        }
    }

    /**
     * Method that delete entity invite from DB if user accept invite
     * @param message
     */
    public void receiveAccept(String message) {
        try {
            String obj = message.substring(6, message.length());
            Invite invite = om.readValue(obj, Invite.class);
            List<Invite> invites = inviteRepo.findAllByInvitedIdAndEvent(invite.getInvitedId(), invite.getEvent());
            if (invites != null) {
                inviteRepo.deleteAll(invites);
            } else {
                log.error("Can't delete " + message);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * send event info to notify-service
     * @param message
     */
    public void sendEvent(String message) {
        try {
            EventDTO eventDTO = om.readValue(message, EventDTO.class);
            saveInvites(getInvites(eventDTO));
            MessageDTO messageDTO = makeMessage(eventDTO);
            redisPublisher.publish(channel, om.writeValueAsString(messageDTO));
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Convert recieved info about event to ready message for notify service
     * @param eventDTO
     * @return messageDTO
     */
    public MessageDTO makeMessage(EventDTO eventDTO) {
        Message message = new Message();
        message.setTitle(eventDTO.getTitle());
        message.setBody(eventDTO.getText());
        message.setDate(eventDTO.getDate());

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUsers(eventDTO.getInvitedIds());
        messageDTO.setMessage(message);
        return messageDTO;
    }

    /**
     * @deprecated
     * Method to parse EventDTO and send messages about event & individual invites separately
     * It is deprecated, use sendEvent(String message)
     * @param message
     */
    public void sendEventT(String message) {
        try {
            EventDTO eventDTO = om.readValue(message, EventDTO.class);
            Event event = new Event();
            BeanUtils.copyProperties(eventDTO, event);

            List<Invite> inviteList = saveInvites(getInvites(eventDTO));
            redisPublisher.publish(channel, "event " + om.writeValueAsString(event));
            for (Invite invite : inviteList) {
                redisPublisher.publish(channel, "invite " + om.writeValueAsString(invite));
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Get the list of invites from event's server message
     * @param eventDTO
     * @return List of invite
     */
    public List<Invite> getInvites(EventDTO eventDTO) {
        List<Invite> inviteList = new ArrayList<>();
        eventDTO.getInvitedIds().forEach(inviteId -> {
            Invite invite = new Invite();
            invite.setInvitedId(inviteId);
            invite.setDate(eventDTO.getDate());
            invite.setEvent(eventDTO.getTitle());
            inviteList.add(invite);
        });
        return inviteList;
    }

    /**
     * Save the list of objects in the database
     * @param invites
     * @return list of invits that was saved in database
     */
    public List<Invite> saveInvites(List<Invite> invites) {
        return inviteRepo.saveAll(invites);
    }
}
