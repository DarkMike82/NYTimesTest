package com.trivago.nytimestest.view.main;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.trivago.nytimestest.BR;
import com.trivago.nytimestest.model.Article;

/**
 * Created by Michael Dontsov on 08.04.2017.
 */

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;

    public ArticleViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
    }

    public void setData(Article article) {
        binding.setVariable(BR.article, article);
        binding.executePendingBindings();
        //TODO check OnClickListener
        ArticleFragment f = ArticleFragment.newInstance(article.getArticleUrl());
        ((MainActivity)itemView.getContext()).StartFragment(f);
    }
}
