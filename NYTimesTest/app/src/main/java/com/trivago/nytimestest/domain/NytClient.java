package com.trivago.nytimestest.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Michael Dontsov on 08.04.2017.
 */

public class NytClient {

    public static final String CHARSET = "UTF-8";
    private static final int CONNECT_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 10;

    private static final String API_KEY = "881fe9dd71d245e394545b25647b0874";

    private static final String TOP_SECTION = "all-sections";
    private static final int TOP_PERIOD = 1;

    private static final String SORT = "newest";

    //host & endpoints
    private static final String HOST = "https://api.nytimes.com";
    private static final String TOP_URL = HOST+"/svc/mostpopular/v2/mostviewed/%s/%d.json";
    private static final String SEARCH_URL = HOST+"/svc/search/v2/articlesearch.json";

    private static NytClient instance = new NytClient();
    public static NytClient getInstance() {return instance;}

    private OkHttpClient okClient;

    private NytClient() {
        okClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public Call getMostViewed(Callback responseCallback){
        StringBuilder url = new StringBuilder()
            .append(String.format(TOP_URL, TOP_SECTION, TOP_PERIOD))
            .append(param(true, "api-key", API_KEY));
        return getAsync(url.toString(), responseCallback);
    }

    public Call searchArticles(String query, String page, Callback responseCallback) {
        StringBuilder url = new StringBuilder(SEARCH_URL)
                .append(param(true, "api-key", API_KEY))
                .append(param(false, "q", query))
                .append(param(false, "sort", SORT))
                .append(param(false, "page", page));
        return getAsync(url.toString(), responseCallback);
    }

    private Call getAsync(String requestUrl, Callback responseCallback){
        Request r = new Request.Builder()
                .url(requestUrl)
                .build();
        Call c = okClient.newCall(r);
        c.enqueue(responseCallback);
        return c;
    }

    /**
     * format raw name-value pair into valid URL param
     * @param first
     * @param name
     * @param value
     * @return
     */
    private static String param(boolean first, String name, Object value) {
        String encodedValue = null;
        try {
            encodedValue = URLEncoder.encode(value.toString(), CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String prefix = first ? "?" : "&";
        return String.format("%s%s=%s", prefix, name, encodedValue);
    }
}
