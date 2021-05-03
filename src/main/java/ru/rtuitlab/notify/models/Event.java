package ru.rtuitlab.notify.models;

import lombok.Data;

@Data
public class Event {
    protected String payment;
    protected String title;
    protected String text;
    protected String date;
}
