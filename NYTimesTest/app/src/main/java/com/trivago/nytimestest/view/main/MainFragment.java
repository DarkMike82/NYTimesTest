package com.trivago.nytimestest.view.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.trivago.nytimestest.R;

public class MainFragment extends Fragment {

    private EditText mSearchField;
    private SwipeRefreshLayout mRefresher;
    private RecyclerView mArticles;
    private ArticleAdapter mAdapter;
    private TextView mEmptyView;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mSearchField = (EditText)v.findViewById(R.id.field_search);
        mRefresher = (SwipeRefreshLayout)v.findViewById(R.id.refresher);
        mArticles = (RecyclerView)v.findViewById(R.id.list_articles);
        mEmptyView = (TextView)v.findViewById(R.id.txt_empty);

        return v;
    }

}
