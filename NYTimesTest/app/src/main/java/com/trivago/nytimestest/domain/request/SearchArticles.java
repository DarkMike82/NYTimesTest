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

public class SearchArticles extends NytRequest {

    public SearchArticles() {
        mRawDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        mResultsKey = "docs";
    }

    @Override
    public void execute(NytCallback callback, Object... params) {
        super.execute(callback, params);
        NytClient.getInstance().searchArticles(
                params[0].toString(),
                params[1].toString(),
                responseCallback()
        );
    }

    @Override
    protected Article parseItem(JSONObject articleJson){
        try {
            Article art = new Article();
            art.setTitle(articleJson.getJSONObject("headline").getString("main"));
            art.setByLine(articleJson.getJSONObject("byline").getString("original"));
            art.setPublishedDate(parseDate(articleJson.getString("pub_date")));
            String[] imageData = parseMedia(articleJson);
            if (imageData != null && imageData.length == 2) {
                art.setImageUrl(imageData[0]);
                art.setImageCaption(imageData[1]);
            }
            art.setArticleAbstract(articleJson.getString("lead_paragraph"));
            art.setArticleUrl(articleJson.getString("web_url"));
            return art;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] parseMedia(JSONObject articleJson) {
        try {
            JSONArray mediaArray = articleJson.getJSONArray("multimedia");
            if (mediaArray == null || mediaArray.length() == 0)
                return null;
            for (int i = 0; i < mediaArray.length(); i++) {
                JSONObject item = mediaArray.getJSONObject(i);
                //check media type
                String type = item.getString("type");
                if (!TYPE_IMAGE.equalsIgnoreCase(type))
                    continue;
                //check media subtype(format)
                String format = item.getString("subtype");
                if (format != null && format.toLowerCase().contains(FORMAT_THUMB)) {
                    return new String[]{
                            item.getString("url"),
                            articleJson.getJSONObject("headline").getString("print_headline")
                    };
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
