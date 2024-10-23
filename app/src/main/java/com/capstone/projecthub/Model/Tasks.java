package com.capstone.projecthub.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tasks {
    public String projectId, userId, taskDetails, status, username;
    public Date dueDate;

    public String dateString() {
        String dateFormat = "dd/MM/yyyy";

        DateFormat df = new SimpleDateFormat(dateFormat);

        return df.format(dueDate);
    }
}
