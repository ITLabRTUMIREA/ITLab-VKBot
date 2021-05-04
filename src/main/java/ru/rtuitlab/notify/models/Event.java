package ru.rtuitlab.notify.models;

import lombok.Data;

@Data
public class Event {
    private String payment;
    private String title;
    private String text;
    private String date;
    private Long size;
}
