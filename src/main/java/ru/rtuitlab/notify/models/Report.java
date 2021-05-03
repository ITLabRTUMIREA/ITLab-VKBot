package ru.rtuitlab.notify.models;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Report {
    private Long senderId;
    private Long receiverId;
    private ZonedDateTime Date = ZonedDateTime.now();
//    private String text;
}
