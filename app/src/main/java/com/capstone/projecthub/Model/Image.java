package com.capstone.projecthub.Model;

public class Image {
    String name, url;

    //will be used in sharing files section

    public Image() {

    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
