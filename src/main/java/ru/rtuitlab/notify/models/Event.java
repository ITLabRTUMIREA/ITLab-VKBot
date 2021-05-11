package ru.rtuitlab.notify.models;

import lombok.Data;

import java.util.List;

@Data
public class Event {
//    private String payment; ???
    private String title;
    private String date;
    private Integer size;
    private List<String> invitedIds;
}
