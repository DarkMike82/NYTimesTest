package com.trivago.nytimestest.view.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trivago.nytimestest.R;
import com.trivago.nytimestest.core.DataManager;
import com.trivago.nytimestest.model.Article;

/**
 * Created by Michael Dontsov on 08.04.2017.
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
            mItemCount = 0;
            mMoreToLoad = true;
            mCurrentPage = -1;
        }
        boolean pageReady = DataManager.getInstance().loadPageAsync(mCurrentPage + 1);
        if (pageReady) {
            onPageLoaded(mCurrentPage + 1);
        }
    }

    public void onPageLoaded(int page) {
        int count = DataManager.getInstance().getPage(page);
        if (count == 0) {
            mMoreToLoad = false;
            return;
        }
        mCurrentPage++;
        mMoreToLoad = count == DataManager.PAGE_SIZE;
        int startItem = mItemCount;
        mItemCount += count;
        notifyItemRangeInserted(startItem, count);
    }
}
