package com.example.soundsaga.api;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.soundsaga.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

public class BookContents {
    private static final String URL = "https://christopherhield.com/ABooks/abook_contents.json";
    private final MainActivity mainActivity;
    private JSONArray audiobooks = null;

    public BookContents(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void getData() {
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                response -> {
                    try {
                        audiobooks = response;
                        mainActivity.runOnUiThread(() -> mainActivity.getBooks(audiobooks));
                    } catch (Exception e) {
                        mainActivity.runOnUiThread(() -> mainActivity.getBooks(null));
                    }
                },
                error -> {
                    mainActivity.runOnUiThread(() -> mainActivity.getBooks(null));
                }) {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers, "UTF-8")
                    );
                    JSONArray jsonResponse = new JSONArray(jsonString);

                    return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response));

                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        MyVolley.getInstance(mainActivity).addToRequestQueue(jsonArrayRequest);
    }

}
