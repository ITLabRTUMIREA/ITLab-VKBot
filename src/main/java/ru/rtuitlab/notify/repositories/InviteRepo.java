package ru.rtuitlab.notify.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rtuitlab.notify.models.Invite;

import java.util.List;

public interface InviteRepo extends JpaRepository<Invite, Long> {
    List<Invite> findAllByInvitedIdAndEvent(Long invitedId, String event);
}
