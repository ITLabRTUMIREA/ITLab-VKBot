package ru.rtuitlab.notify.models;

import lombok.Data;

@Data
public class Report {
    private String sender;
    private String receiverId;
    private String date;
//    private String text;
}
