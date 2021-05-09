package ru.rtuitlab.notify.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "invites")
public class Invite {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String invitedId;
    private String event;
    private String date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invite invite = (Invite) o;
        return invitedId.equals(invite.invitedId) && event.equals(invite.event) && Objects.equals(date, invite.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invitedId, event, date);
    }
}
