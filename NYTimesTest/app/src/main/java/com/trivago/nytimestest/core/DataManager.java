package com.trivago.nytimestest.core;

import com.trivago.nytimestest.domain.NytCallback;
import com.trivago.nytimestest.domain.request.GetMostViewed;
import com.trivago.nytimestest.domain.request.SearchArticles;
import com.trivago.nytimestest.model.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Dontsov on 08.04.2017.
 */

public class DataManager {
    public static final int PAGE_SIZE = 10;

    private static DataManager instance = new DataManager();
    public static DataManager getInstance() {return instance;}

    private List<Article> mMostViewedList;
    private List<Article> mSearchResults;
    private String mQuery;

    private GetMostViewed mMostViewedRequest;
    private SearchArticles mSearchRequest;
    private NytCallback mCallback;

    private DataManager() {
        mMostViewedList = new ArrayList<>();
        mSearchResults = new ArrayList<>();

        mMostViewedRequest = new GetMostViewed();
        mSearchRequest = new SearchArticles();
    }

    public void setCallback(NytCallback callback) {
        mCallback = callback;
    }

    public String getQuery() {return mQuery;}

    public void setQuery(String query) {
        mQuery = query;
    }

    public void reset() {
        mQuery = null;
        mMostViewedList.clear();
        mSearchResults.clear();
    }

    /**
     * Returns item count for requested page
     * @param page
     * @return
     */
    public int getPage(int page) {
        int availableItems = 0;
        if (mQuery != null) {
            //take new page from SearchResults
            availableItems = mSearchResults.size()%(page*10);
        }
        else {
            //take new page from MostViewed
            availableItems = mMostViewedList.size()%(page*10);
        }

        return availableItems > PAGE_SIZE ? PAGE_SIZE : availableItems;
    }

    /**
     * Load items fro requested page.
     * @param page zero-based page number
     * @return true if load performed synchronously
     */
    public boolean loadPageAsync(int page) {
        if (mQuery != null) {
            loadSearchResults(page);
            return false;
        }
        return true;
    }

    public Article getArticle(int position) {
        if (mQuery != null)
            return mSearchResults.get(position);
        else
            return mMostViewedList.get(position);
    }

    public void loadMostViewed(){
        mMostViewedRequest.execute(mCallback);
    }

    public void loadSearchResults(int page) {
        mSearchRequest.cancel();
        mSearchRequest.execute(mCallback, mQuery, page);
    }

    public void saveResult(List<Article> result) {
        if (mQuery != null)
            mSearchResults.addAll(result);
        else
            mMostViewedList.addAll(result);
    }
}
