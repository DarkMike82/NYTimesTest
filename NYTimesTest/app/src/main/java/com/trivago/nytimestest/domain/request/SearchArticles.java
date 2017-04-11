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
 * Request for article search
 */

public class SearchArticles extends NytRequest {
    private static final String NYT_FILE_HOST = "https://static01.nyt.com/";
    private static final String DATE_PATTERN1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String DATE_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public SearchArticles() {
        mRawDatePattern = DATE_PATTERN1;
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
    protected List<Article> parseJson(String jsonString) {
        List<Article> articles;
        try {
            JSONObject response = getObject(new JSONObject(jsonString), "response");
            JSONArray items = response == null ? null : getArray(response, "docs");
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
            art.setTitle(getObjectField(articleJson, "headline", "main"));
            art.setByLine(getObjectField(articleJson, "byline", "original"));
            //some article dates contain char-based zero-hour offset, instead of numbers
            String dateString = articleJson.getString("pub_date");
            if (dateString.endsWith("Z"))
                mRawDatePattern = DATE_PATTERN2;
            else
                mRawDatePattern = DATE_PATTERN1;
            art.setPublishedDate(parseDate(dateString));
            String[] imageData = parseMedia(articleJson);
            if (imageData != null && imageData.length == 2) {
                art.setImageUrl(imageData[0]);
                art.setImageCaption(imageData[1]);
            }
            if (!articleJson.isNull("lead_paragraph"))
                art.setArticleAbstract(articleJson.getString("lead_paragraph"));
            art.setArticleUrl(articleJson.getString("web_url"));
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
            JSONArray mediaArray = getArray(articleJson, "multimedia");
            if (mediaArray == null)
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
                            NYT_FILE_HOST+item.getString("url"),
                            getObjectField(articleJson, "headline", "print_headline")
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
