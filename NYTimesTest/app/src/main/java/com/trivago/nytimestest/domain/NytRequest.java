package com.trivago.nytimestest.domain;

import com.trivago.nytimestest.core.DataManager;
import com.trivago.nytimestest.core.NytApp;
import com.trivago.nytimestest.model.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Michael Dontsov on 08.04.2017.
 */

public abstract class NytRequest {
    protected static final String TYPE_IMAGE = "image";
    protected static final String FORMAT_THUMB = "thumbnail";

    private static final String APP_DATE_PATTERN = "MMMMM d, yyyy";
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat();

    private NytCallback callback;

    protected String mErrorMsg;
    protected Call currentCall;

    protected String mRawDatePattern;
    protected String mResultsKey;

    public void execute(NytCallback callback, Object... params){
        this.callback = callback;
    }

    private List<Article> parseJson(String jsonString) {
        List<Article> articles;
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray items = json.getJSONArray(mResultsKey);
            if (items == null || items.length() == 0)
                return null;
            articles = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                Article art = parseItem(items.getJSONObject(i));
                if (art != null)
                    articles.add(art);
            }
            return articles;
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    protected abstract Article parseItem(JSONObject articleJson);

    public String getErrorMsg() {
        return mErrorMsg;
    }

    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
        }
    }

    private String inflateString(byte[] bytes) {
        String data = null;
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        byte[] inflatedBytes = new byte[bytes.length];
        try {
            int resultLength = inflater.inflate(inflatedBytes);
            inflater.end();
            data = new String(inflatedBytes, 0, resultLength, NytClient.CHARSET);
        } catch (DataFormatException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }

    protected final Callback responseCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mErrorMsg = e.getMessage();
                NytApp.RunOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = null;
                try {
                    String contentEncoding = response.headers().get("Content-Encoding");

                    //check if response is encoded
                    if (contentEncoding != null && contentEncoding.contains("deflate"))
                        responseString = inflateString(response.body().bytes());
                    else
                        responseString = new String(response.body().bytes(), NytClient.CHARSET);

                    //parse
                    final List<Article> result;
                    if (response.isSuccessful()) {
                        mErrorMsg = null;
                        result = parseJson(responseString);
                        if (result != null)
                            DataManager.getInstance().saveResult(result);
                    }
                    else {
                        result = null;
                        JSONObject json = new JSONObject(responseString);
                        mErrorMsg = json.getString("message");
                    }

                    //notify UI
                    NytApp.RunOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(result != null);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    mErrorMsg = responseString;
                    NytApp.RunOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(false);
                        }
                    });
                }
                response.close();
            }
        };
    }

    protected String parseDate(String dateString){
        sDateFormat.applyPattern(mRawDatePattern);
        try {
            Date pubDate = sDateFormat.parse(dateString);
            sDateFormat.applyPattern(APP_DATE_PATTERN);
            return sDateFormat.format(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
