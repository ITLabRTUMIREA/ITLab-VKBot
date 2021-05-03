package ru.rtuitlab.notify.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
public class EventDTO extends Event{
//    @JsonIgnore
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private List<String> invitedIds;
//    private String payment;
//    private String title;
//    private String text;
//    private String date;
    private List<Long> invitedIds;
}
