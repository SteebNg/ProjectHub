package com.capstone.projecthub.Model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Announcement implements Serializable {
    public String announcementId, projectId, title, body, announcerId, announcerName;
    public Date date;

    public String dateTimeString() {
        String dateFormat = "dd/MM/yyyy HH:mm:ss";

        DateFormat df = new SimpleDateFormat(dateFormat);

        return df.format(date);
    }

    public String dateString() {
        String dateFormat = "dd/MM/yyyy";

        DateFormat df = new SimpleDateFormat(dateFormat);

        return df.format(date);
    }
}
