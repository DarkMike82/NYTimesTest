package com.trivago.nytimestest.view.main;


import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.trivago.nytimestest.R;
import com.trivago.nytimestest.core.PopupController;

/**
 * A {@link Fragment} to display article details.
 */
public class ArticleFragment extends Fragment {
    private static final String EXTRA_URL = "article_url";
    private static final String NYT_HOST = "nytimes.com";

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
        super.onStart();
        ActionBar ab = ((AppCompatActivity)getActivity())
                .getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.title_article);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        String url = getArguments().getString(EXTRA_URL);
        if (TextUtils.isEmpty(url) ||
            !Patterns.WEB_URL.matcher(url).matches()) {
            PopupController.showMessage(getActivity(), R.string.invalid_url);
            return;
        }
        mBrowserView.setWebViewClient(new NytWebViewClient());
//        mBrowserView.getSettings().setJavaScriptEnabled(true);
        mBrowserView.loadUrl(url);
        PopupController.showProgress(getActivity(), R.string.loading);
    }

    private class NytWebViewClient extends WebViewClient {
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();
            return !(host != null && host.toLowerCase().contains(NYT_HOST));
        }

        @Override
        @TargetApi(21)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String host = request.getUrl().getHost();
            return !(host != null && host.toLowerCase().contains(NYT_HOST));
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            PopupController.hideProgress();
        }
    }
}
