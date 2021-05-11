package ru.rtuitlab.notify.models;

import lombok.Data;

@Data
public class Report {
    private String senderId;
    private String receiverId;
    private String date;
}
