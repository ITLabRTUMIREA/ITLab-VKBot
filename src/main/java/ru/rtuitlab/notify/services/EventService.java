package ru.rtuitlab.notify.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtuitlab.notify.models.*;
import ru.rtuitlab.notify.redis.RedisPublisher;
import ru.rtuitlab.notify.repositories.InviteRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService implements MessageHandler{

    private final RedisPublisher redisPublisher;
    private final InviteRepo inviteRepo;
    private final UserService userService;
    private final ObjectMapper om;

    public EventService(RedisPublisher redisPublisher, InviteRepo inviteRepo, UserService userService, ObjectMapper om) {
        this.redisPublisher = redisPublisher;
        this.inviteRepo = inviteRepo;
        this.userService = userService;
        this.om = om;
    }

    @Value("${database.redis.sendChannel}")
    private String channel;


    @Override
    public void handleMessage(String message) {
        System.out.println("EventService here! + " + message);
        if (message.substring(0, 6).equals("accept")) {
            receiveAccept(message);
        }
        else {
            sendMessage(message);
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
            List<Invite> invites = inviteRepo.findAllByInvitedIdAndEvent(
                    invite.getInvitedId(), invite.getEvent());
            if (invites != null) {
                inviteRepo.deleteAll(invites);
                log.info("Пользователь " + invite.getInvitedId()
                        + " подтвердил участие на мероприятии '" + invite.getEvent() + "'");
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
    @Override
    public void sendMessage(String message) {
        try {
            Event event = om.readValue(message, Event.class);

            sendPersonalInvites(event);

            if (event.getInvitedIds().size() < event.getSize()) {
                sendPublicInvites(event);
            }

        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void sendPublicInvites(Event event) throws JsonProcessingException {
        List<String> usersIds = userService.getUsers()
                .stream()
                .map(User::getId)
                .filter(id -> !event.getInvitedIds().contains(id))
                .collect(Collectors.toList());
        event.setInvitedIds(usersIds);
        MessageDTO messageDTO = makeMessage(
                event, "Появилось новое свободное событие '" + event.getTitle() + "'");
        redisPublisher.publish(channel, om.writeValueAsString(messageDTO));
        log.info("send public invites about " + messageDTO.getMessage().getTitle() + " event");
    }

    private void sendPersonalInvites(Event event) throws JsonProcessingException {
        saveInvites(getInvites(event));
        MessageDTO messageDTO = makeMessage(
                event, "Вас пригласили на мероприятие '" + event.getTitle() + "'");
        redisPublisher.publish(channel, om.writeValueAsString(messageDTO));
        log.info("send personal invites about " + messageDTO.getMessage().getTitle() + " event");
    }



    /**
     * Convert recieved info about event to ready message for notify service
     * @param event
     * @return messageDTO
     */
    public MessageDTO makeMessage(Event event, String text) {
        Message message = new Message();
        message.setTitle(event.getTitle());
        message.setBody(text);
        message.setDate(event.getDate());

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUsers(event.getInvitedIds());
        messageDTO.setMessage(message);
        return messageDTO;
    }

    /**
     * Get the list of invites from event's server message
     * @param event
     * @return List of invite
     */
    public List<Invite> getInvites(Event event) {
        List<Invite> inviteList = new ArrayList<>();
        event.getInvitedIds().forEach(inviteId -> {
            Invite invite = new Invite();
            invite.setInvitedId(inviteId);
            invite.setDate(event.getDate());
            invite.setEvent(event.getTitle());
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
