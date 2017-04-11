package com.trivago.nytimestest.view.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.trivago.nytimestest.R;
import com.trivago.nytimestest.core.DataManager;
import com.trivago.nytimestest.core.PopupController;
import com.trivago.nytimestest.domain.NytCallback;

public class MainFragment extends Fragment implements NytCallback {
    private static final int SEARCH_THRESHOLD = 5;

    private EditText mSearchField;
    private SwipeRefreshLayout mRefresher;
    private RecyclerView mArticles;
    private LinearLayoutManager mLayoutManager;
    private ArticleAdapter mAdapter;
    private TextView mEmptyView;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager.getInstance().setCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mSearchField = (EditText)v.findViewById(R.id.field_search);
        mRefresher = (SwipeRefreshLayout)v.findViewById(R.id.refresher);
        mArticles = (RecyclerView)v.findViewById(R.id.list_articles);
        mEmptyView = (TextView)v.findViewById(R.id.txt_empty);

        setupViews();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar ab = ((AppCompatActivity)getActivity())
                .getSupportActionBar();
        if (ab == null) return;
        ab.setTitle(R.string.app_name);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onResponse(boolean successful) {
        PopupController.hideProgress();
        mRefresher.setRefreshing(false);
        if (successful) {
            mAdapter.onPageLoaded();
            mEmptyView.setVisibility(mAdapter.getItemCount() == 0
                                    ? View.VISIBLE
                                    : View.GONE);
        }
        else
            PopupController.showMessage(getActivity(), DataManager.getInstance().getErrorMessage());
    }

    private void setupViews() {
        String query = DataManager.getInstance().getQuery();
        mSearchField.setText(query == null ? "" : query);
        mSearchField.addTextChangedListener(new TextWatcher() {

            private CharSequence prevQuery;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevQuery = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                performSearch(s, prevQuery);
            }
        });

        mRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataManager.getInstance().clear();
                mSearchField.setText("");
            }
        });

        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mArticles.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        mArticles.addItemDecoration(itemDecoration);
        if (mAdapter == null) {
            mAdapter = new ArticleAdapter();
        }
        if (mAdapter.getItemCount() == 0) {
            mAdapter.loadNextPage(true);
        }
        else {
            mEmptyView.setVisibility(View.GONE);
        }
        mArticles.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void performSearch(CharSequence query, CharSequence prevQuery) {
        //if curr/prev query is below threshold - do nothing
        if (query.length() < SEARCH_THRESHOLD && prevQuery.length() < SEARCH_THRESHOLD)
            return;
        //if newly entered characters are whitespaces - do nothing
        if (query.toString().trim().equalsIgnoreCase(prevQuery.toString()))
            return;
        //if text has been shortened below threshold - hide search results and show most viewed
        if (query.length() < SEARCH_THRESHOLD && prevQuery.length() >= SEARCH_THRESHOLD) {
            DataManager.getInstance().resetQuery();
            mAdapter.loadNextPage(true);
            return;
        }
        //stop scrolling (to avoid exceptions) and scroll to top (if needed)
        mArticles.stopScroll();
        if (mLayoutManager.findFirstCompletelyVisibleItemPosition() > 0)
            mArticles.scrollToPosition(0);
        //reset adapter page counter
        mAdapter.resetPage();
        //start search
        DataManager.getInstance().searchArticles(query);
    }
}
