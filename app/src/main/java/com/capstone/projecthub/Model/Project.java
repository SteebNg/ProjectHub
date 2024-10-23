package com.capstone.projecthub.Model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Project implements Serializable {
    public String projectName, projectDescription, projectId, projectLeaderId, projectImage, projectColor;
    public Date dueDate;
    public String[] memberList;

    public Project() {

    }

    public String dateString() {
        String dateFormat = "dd/MM/yyyy";

        DateFormat df = new SimpleDateFormat(dateFormat);

        return df.format(dueDate);
    }
}
