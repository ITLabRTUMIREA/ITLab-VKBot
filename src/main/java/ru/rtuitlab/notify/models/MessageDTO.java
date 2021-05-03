package ru.rtuitlab.notify.models;

import lombok.Data;

import java.util.List;

@Data
public class MessageDTO {

    private Message message;
    private List<Long> users;
}
