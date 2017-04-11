package com.trivago.nytimestest.domain.request;

import com.trivago.nytimestest.domain.NytCallback;
import com.trivago.nytimestest.domain.NytClient;
import com.trivago.nytimestest.domain.NytRequest;
import com.trivago.nytimestest.model.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for most-viewed articles
 */

public class GetMostViewed extends NytRequest {

    public GetMostViewed() {
        mRawDatePattern = "yyyy-MM-dd";
    }

    @Override
    public void execute(NytCallback callback, Object... params) {
        super.execute(callback, params);
        NytClient.getInstance().getMostViewed(responseCallback());
    }

    @Override
    protected List<Article> parseJson(String jsonString) {
        List<Article> articles;
        try {
            JSONArray items = getArray(new JSONObject(jsonString), "results");
            if (items == null)
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
            mErrorMsg = e.getMessage();
        }
        return null;
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
            mErrorMsg = e.getMessage();
        }
        return null;
    }

    @Override
    protected String[] parseMedia(JSONObject articleJson) {
        try {
            JSONArray mediaArray = getArray(articleJson, "media");
            if (mediaArray == null)
                return null;
            JSONObject media = mediaArray.getJSONObject(0);
            String mediaType = media.getString("type");
            if (!TYPE_IMAGE.equalsIgnoreCase(mediaType))
                return null;
            JSONArray metadata = getArray(media, "media-metadata");
            if (metadata == null)
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
            mErrorMsg = e.getMessage();
        }
        return null;
    }
}
