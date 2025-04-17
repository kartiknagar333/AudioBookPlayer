package com.example.soundsaga.fetcher;

import com.example.soundsaga.model.Book;
import com.example.soundsaga.model.Chapter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetAudiobook {
    private static volatile GetAudiobook instance = null;
    private static final List<Book> audiobooks = new ArrayList<>();
    private static final List<Chapter> chapters = new ArrayList<>();

    private GetAudiobook() {
        final ObjectMapper mapper = new ObjectMapper();
    }

    public static GetAudiobook getInstance() {
        if (instance == null) {
            synchronized (GetAudiobook.class) {
                if (instance == null) {
                    instance = new GetAudiobook();
                }
            }
        }
        return instance;
    }


    public static List<Book> getAudiobooks(final JSONArray array) {
        audiobooks.clear();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);

                List<Chapter> chapters = new ArrayList<>();
                JSONArray contents = jsonObject.getJSONArray("contents");
                for (int c = 0; c < contents.length(); c++) {
                    JSONObject chapterObj = contents.getJSONObject(c);
                    chapters.add(new Chapter(
                            chapterObj.getInt("number"),
                            chapterObj.getString("title"),
                            chapterObj.getString("url")
                    ));
                }

                audiobooks.add(new Book(
                        jsonObject.getString("title"),
                        jsonObject.getString("author"),
                        jsonObject.getString("date"),
                        jsonObject.getString("language"),
                        jsonObject.getString("duration"),
                        jsonObject.getString("image"),
                        chapters
                ));

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return audiobooks;
    }



}
