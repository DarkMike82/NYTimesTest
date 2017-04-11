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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Base class for NYT API requests
 */

public abstract class NytRequest {
    protected static final String TYPE_IMAGE = "image";
    protected static final String FORMAT_THUMB = "thumbnail";

    private static final String MSG_LIMIT = "API rate limit exceeded";

    private static final String APP_DATE_PATTERN = "MMMM d, yyyy";
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat(APP_DATE_PATTERN, Locale.UK);

    private NytCallback callback;

    protected String mErrorMsg;
    protected Call currentCall;

    protected String mRawDatePattern;

    public void execute(NytCallback callback, Object... params){
        this.callback = callback;
    }

    protected abstract List<Article> parseJson(String jsonString);

    protected abstract Article parseItem(JSONObject articleJson);

    protected abstract String[] parseMedia(JSONObject articleJson);

    public String getErrorMsg() {
        return mErrorMsg;
    }

    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
        }
    }

    /**
     * Decode request contents
     * @param bytes
     * @return
     */
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

    /**
     * Create callback for request
     * @return
     */
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
                String contentEncoding = response.headers().get("Content-Encoding");

                //check if response is encoded
                if (contentEncoding != null && contentEncoding.contains("deflate"))
                    responseString = inflateString(response.body().bytes());
                else
                    responseString = new String(response.body().bytes(), NytClient.CHARSET);

                //parse
                if (response.isSuccessful()) {
                    mErrorMsg = null;
                    List<Article> result = parseJson(responseString);
                    if (result != null) {
                        DataManager.getInstance().saveResult(result);
                    }
                }
                else {
                    mErrorMsg = response.code()+" "+response.message();
                    try {
                        String msg = new JSONObject(responseString).getString("message");
                        //skip "API-limit" responses
                        if (MSG_LIMIT.equalsIgnoreCase(msg)) {
                            mErrorMsg = null;
                        } else {
                            mErrorMsg += "\n"+msg;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mErrorMsg = responseString;
                    }
                }

                //notify UI
                NytApp.RunOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(mErrorMsg == null);
                    }
                });
                response.close();
            }
        };
    }

    /**
     * Convert API date string to valid-formatted date string
     * @param dateString
     * @return
     */
    protected String parseDate(String dateString) {
        sDateFormat.applyPattern(mRawDatePattern);
        try {
            Date pubDate = sDateFormat.parse(dateString);
            sDateFormat.applyPattern(APP_DATE_PATTERN);
            return sDateFormat.format(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
            mErrorMsg = e.getMessage();
        }
        return "";
    }

    /**
     * Get a JSON object that is a part of another JSON
     * @param json
     * @param objectField
     * @return
     */
    public JSONObject getObject(JSONObject json, String objectField) {
        if (json == null || !json.has(objectField) || json.isNull(objectField))
            return null;
        try {
            Object obj = json.get(objectField);
            return obj instanceof JSONObject
                    ? (JSONObject)obj
                    : null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get non-empty array (if available) from by field name
     * @param json
     * @param arrayField
     * @return
     */
    public JSONArray getArray(JSONObject json, String arrayField) {
        if (json == null || !json.has(arrayField) || json.isNull(arrayField))
            return null;
        try {
            Object obj = json.get(arrayField);
            return (obj instanceof JSONArray && ((JSONArray)obj).length() > 0)
                    ? (JSONArray)obj
                    : null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a string value from JSON object that is a part of another JSON
     * @param json
     * @param objectField
     * @param field
     * @return
     */
    public String getObjectField(JSONObject json, String objectField, String field) {
        JSONObject obj = getObject(json, objectField);
        if (obj == null || !obj.has(field) || obj.isNull(field))
            return null;
        try {
            return obj.getString(field);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
