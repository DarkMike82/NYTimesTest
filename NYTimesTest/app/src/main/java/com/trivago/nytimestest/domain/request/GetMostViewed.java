package com.trivago.nytimestest.domain.request;

import com.trivago.nytimestest.domain.NytCallback;
import com.trivago.nytimestest.domain.NytClient;
import com.trivago.nytimestest.domain.NytRequest;
import com.trivago.nytimestest.model.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Michael Dontsov on 08.04.2017.
 */

public class GetMostViewed extends NytRequest {

    public GetMostViewed() {
        mRawDatePattern = "yyyy-MM-dd";
        mResultsKey = "results";
    }

    @Override
    public void execute(NytCallback callback, Object... params) {
        super.execute(callback, params);
        NytClient.getInstance().getMostViewed(responseCallback());
    }

    @Override
    protected Article parseItem(JSONObject articleJson){
        try {
            Article art = new Article();
            art.setTitle(articleJson.getString("title"));
            art.setByLine(articleJson.getString("byline"));
            art.setPublishedDate(parseDate(articleJson.getString("published_date")));
            String[] imageData = parseMedia(articleJson);
            if (imageData != null && imageData.length == 2) {
                art.setImageUrl(imageData[0]);
                art.setImageCaption(imageData[1]);
            }
            art.setArticleAbstract(articleJson.getString("abstract"));
            art.setArticleUrl(articleJson.getString("url"));
            return art;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] parseMedia(JSONObject articleJson) {
        try {
            JSONObject media = articleJson.getJSONArray("media")
                    .getJSONObject(0);
            String mediaType = media.getString("type");
            if (!TYPE_IMAGE.equalsIgnoreCase(mediaType))
                return null;
            JSONArray metadata = media.getJSONArray("media-metadata");
            if (metadata == null || metadata.length() == 0)
                return null;
            for (int i = 0; i < metadata.length(); i++) {
                JSONObject item = metadata.getJSONObject(i);
                String format = item.getString("format");
                if (format != null && format.toLowerCase().contains(FORMAT_THUMB)) {
                    return new String[]{
                            item.getString("url"),
                            media.getString("caption")
                    };
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
