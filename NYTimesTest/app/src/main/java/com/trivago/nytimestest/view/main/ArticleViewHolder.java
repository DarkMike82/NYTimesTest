package com.trivago.nytimestest.view.main;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.trivago.nytimestest.BR;
import com.trivago.nytimestest.model.Article;

/**
 * View holder for Article item
 */

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;

    public ArticleViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
    }

    public void setData(final Article article) {
        binding.setVariable(BR.article, article);
        binding.executePendingBindings();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArticleFragment f = ArticleFragment.newInstance(article.getArticleUrl());
                ((MainActivity)itemView.getContext()).StartFragment(f);
            }
        });
    }
}
