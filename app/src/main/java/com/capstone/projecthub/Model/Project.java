package com.capstone.projecthub.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Project {
    public String projectName, projectDescription;
    public Date dueDate;

    public Project() {

    }

    public String dateString() {
        String dateFormat = "dd/MM/yyyy";

        DateFormat df = new SimpleDateFormat(dateFormat);

        return df.format(dueDate);
    }
}
