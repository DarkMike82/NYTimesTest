package com.trivago.nytimestest.view.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trivago.nytimestest.R;
import com.trivago.nytimestest.core.DataManager;
import com.trivago.nytimestest.model.Article;

/**
 * Adapter for articles list.
 * To avoid redundant object refs - items acquired directly from DataManager according to page settings
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleViewHolder> {
    private int mItemCount = 0;
    private boolean mMoreToLoad = true;
    private int mCurrentPage = -1;

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        Article item = DataManager.getInstance().getArticle(position);
        holder.setData(item);
        if (position == mItemCount-1 && mMoreToLoad)
            loadNextPage(false);
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public void loadNextPage(boolean reset) {
        if (reset) {
            resetPage();
        }
        DataManager.getInstance().loadPageAsync(mCurrentPage + 1);
    }

    public void onPageLoaded() {
        int itemCount = DataManager.getInstance().getPage(mCurrentPage + 1);
        mMoreToLoad = itemCount == DataManager.PAGE_SIZE;
        if (itemCount == 0) {
            return;
        }
        mCurrentPage++;
        mItemCount += itemCount;
        notifyDataSetChanged();
    }

    public void resetPage() {
        mCurrentPage = -1;
        mItemCount = 0;
        mMoreToLoad = true;
    }
}
