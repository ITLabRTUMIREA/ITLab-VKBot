package ru.rtuitlab.notify.services;

import org.springframework.stereotype.Service;

@Service("reports")
public class ReportService implements MessageHandler{

//    public void processReports() {
//
//    }

    @Override
    public void handleMessage(String message) {
        System.out.println("ReportService here! + " + message);
    }
}
