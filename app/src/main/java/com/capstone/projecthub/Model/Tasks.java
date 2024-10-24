package com.capstone.projecthub.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Tasks {
    public String projectId, status, tasksName, tasksDesc;
    public Date dueDate, assignedDate;
    public ArrayList<String> usersId;

    public String dateStringDueDate() {
        String dateFormat = "dd/MM/yyyy";

        DateFormat df = new SimpleDateFormat(dateFormat);

        return df.format(dueDate);
    }

    public String dateStringAssignedDate() {
        String dateFormat = "dd/MM/yyyy";

        DateFormat df = new SimpleDateFormat(dateFormat);

        return df.format(assignedDate);
    }
}
