package com.example.soundsaga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class Chapter implements Serializable {

    @JsonProperty("number")
    private final int number;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("url")
    private final String url;

    public Chapter(@JsonProperty("number") int number,
                   @JsonProperty("title") String title,
                   @JsonProperty("url") String url) {
        this.number = number;
        this.title = title;
        this.url = url;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

}

