package com.trivago.nytimestest.core;

import android.os.Handler;

import com.trivago.nytimestest.domain.NytCallback;
import com.trivago.nytimestest.domain.request.GetMostViewed;
import com.trivago.nytimestest.domain.request.SearchArticles;
import com.trivago.nytimestest.model.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents request manager & data provider for UI
 */

public class DataManager {
    public static final int PAGE_SIZE = 10;
    private static final long SEARCH_PERIOD = 500;

    private static DataManager instance = new DataManager();
    public static DataManager getInstance() {return instance;}

    private List<Article> mMostViewedList;
    private List<Article> mSearchResults;
    private String mQuery;

    private GetMostViewed mMostViewedRequest;
    private SearchArticles mSearchRequest;
    private NytCallback mCallback;

    //article search
    private boolean mShouldResetList;
    private Runnable mSearchRunnable;
    private Handler mSearchHandler;


    private DataManager() {
        mMostViewedList = new ArrayList<>();
        mMostViewedRequest = new GetMostViewed();

        mSearchResults = new ArrayList<>();
        mSearchRequest = new SearchArticles();
        mSearchHandler = new Handler();
    }

    public void setCallback(NytCallback callback) {
        mCallback = callback;
    }

    public String getQuery() {return mQuery;}

    public void clear() {
        mMostViewedList.clear();
        resetQuery();
    }

    public void resetQuery() {
        mQuery = null;
        mSearchResults.clear();
    }

    /**
     * Calculates item count for requested page
     * @param page zero-based page number
     * @return number of items
     */
    public int getPage(int page) {
        int listSize = mQuery == null
                ? mMostViewedList.size()
                : mSearchResults.size();
        int availableItems = listSize - (page*PAGE_SIZE);

        return availableItems > PAGE_SIZE ? PAGE_SIZE : availableItems;
    }

    /**
     * Load items for requested page.
     * @param page zero-based page number
     * @return true if load performed synchronously
     */
    public boolean loadPageAsync(int page) {
        //if filter is enabled - check search results
        if (mQuery != null) {
            //if requested page is already loaded - notify & return
            if (mSearchResults.size() - page*PAGE_SIZE > 0) {
                mCallback.onResponse(true);
                return true;
            }
            loadSearchResults(page);
            return false;
        }
        //if filter is disabled and data lists are empty - send most-viewed request
        if (mMostViewedList.isEmpty()) {
            mMostViewedRequest.execute(mCallback);
            return false;
        }
        //if filter is disabled and most-viewed are already loaded - notify & return
        mCallback.onResponse(true);
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
        mSearchRequest.execute(mCallback, mQuery.trim(), page);
    }

    /**
     * Append received articles to appropriate data list
     * @param result
     */
    public void saveResult(List<Article> result) {
        if (mQuery != null) {
            if (mShouldResetList && result.size() > 0)
                mSearchResults.clear();
            mSearchResults.addAll(result);
            mShouldResetList = false;
        } else
            mMostViewedList.addAll(result);
    }

    public String getErrorMessage() {
        return mQuery == null
                ? mMostViewedRequest.getErrorMsg()
                : mSearchRequest.getErrorMsg();
    }

    public void searchArticles(final CharSequence query) {
        //previous request: cancel API call and remove task from pool
        mSearchRequest.cancel();
        if (mSearchRunnable != null)
            mSearchHandler.removeCallbacks(mSearchRunnable);
        //"thread pool" - due to NYT API limit (5 calls per second), requests performed with delay
        mSearchRunnable = new Runnable() {
            @Override
            public void run() {
                mQuery = query.toString();
                loadSearchResults(0);
                mShouldResetList = true;
            }
        };
        mSearchHandler.postDelayed(mSearchRunnable, SEARCH_PERIOD);
    }
}
