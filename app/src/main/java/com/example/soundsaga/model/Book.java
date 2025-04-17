package com.example.soundsaga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Book implements Serializable {

    @JsonProperty("title")
    private final String title;

    @JsonProperty("author")
    private final String author;

    @JsonProperty("date")
    private final String date;

    @JsonProperty("language")
    private final String language;

    @JsonProperty("duration")
    private final String duration;

    @JsonProperty("image")
    private final String image;

    @JsonProperty("contents")
    private final List<Chapter> contents;

    private String lastdatetime;
    private String lasttime;
    private String chapter;

    public Book(@JsonProperty("title") String title,
                     @JsonProperty("author") String author,
                     @JsonProperty("date") String date,
                     @JsonProperty("language") String language,
                     @JsonProperty("duration") String duration,
                     @JsonProperty("image") String image,
                     @JsonProperty("contents") List<Chapter> contents) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.language = language;
        this.duration = duration;
        this.image = image;
        this.contents = contents;
    }

    public void saveInhistory(String currentTime,String duration,String ch){
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy HH:mm:ss", Locale.getDefault());
        lastdatetime = sdf.format(new Date());
        if(duration.split(":")[0].equals("00")){
            lasttime = currentTime.split(":")[1] + ":" + currentTime.split(":")[2] + " of " + duration.split(":")[1]
            + ":" + duration.split(":")[2];
        }else {
            lasttime = currentTime + " of " + duration;
        }
        chapter = ch;
    }

    public static void sortBooksByLastdatetime(List<Book> books) {
        final SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy HH:mm:ss", Locale.getDefault());
        Collections.sort(books, new Comparator<Book>() {
            @Override
            public int compare(Book b1, Book b2) {
                try {
                    Date date1 = sdf.parse(b1.getLastdate());
                    Date date2 = sdf.parse(b2.getLastdate());
                    assert date2 != null;
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }

    public String getChapter() {
        return chapter;
    }

    public String getLastdatetime() {
        return lastdatetime.substring(0, lastdatetime.length() - 3);
    }

    public String getLastdate() {
        return lastdatetime;
    }

    public String getLasttime() {
        return lasttime;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getLanguage() {
        return language;
    }

    public String getDuration() {
        return duration;
    }

    public String getImage() {
        return image;
    }

    public List<Chapter> getContents() {
        return contents;
    }

}

