package com.trivago.nytimestest.view.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.trivago.nytimestest.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment {
    private static final String EXTRA_URL = "article_url";

    private WebView mBrowserView;

    public static ArticleFragment newInstance(String articleUrl) {
        ArticleFragment f = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_URL, articleUrl);
        f.setArguments(args);
        return f;
    }

    public ArticleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_article, container, false);
        mBrowserView = (WebView)v.findViewById(R.id.browser);
        return v;
    }

    @Override
    public void onStart() {
        //TODO set up title and Back button
        super.onStart();
        String url = getArguments().getString(EXTRA_URL);
        if (TextUtils.isEmpty(url))
            return;
        if (Patterns.WEB_URL.matcher(url).matches())
            return;
        mBrowserView.loadUrl(url);
    }
}
