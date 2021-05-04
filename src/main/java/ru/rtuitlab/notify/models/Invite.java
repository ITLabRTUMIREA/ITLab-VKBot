package ru.rtuitlab.notify.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "invites")
public class Invite {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String invitedId;
//    @ManyToOne
    private String event;
    private String date;
//    @JsonIgnore
//    private Boolean accepted = false;
}
