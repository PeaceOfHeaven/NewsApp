package evich.newsapp.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import evich.newsapp.R;
import evich.newsapp.data.News;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 18/05/2016.
 */
public class NewsFragment extends Fragment implements NewsContract.View {

    public static final String CHANNEL_ARGUMENT_KEY = "channel";

    private NewsContract.Presenter mPresenter;

    @BindView(R.id.root_layout)
    ViewGroup rootLayout;

    @BindView(R.id.news_recyclerView)
    LoadMoreRecylerView mNewsRecylerView;

    @BindView(R.id.swiperefresh)
    ScrollChildSwipeRefreshLayout mSwipeRefreshLayout;

    private NewsAdapter mNewsAdapter;
    private String mChannel;

    public static Fragment getInstance(@NonNull String channel) {
        checkNotNull(channel);

        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(CHANNEL_ARGUMENT_KEY, channel);
        fragment.setArguments(args);
        return fragment;
    }

    public NewsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorAccent));
        boolean screenSmall = getResources().getBoolean(R.bool.screen_small);

        RecyclerView.LayoutManager layoutManager;
        if (screenSmall) {
            layoutManager = new LinearLayoutManager(getActivity());
            mNewsRecylerView.addItemDecoration(new SpaceItemDecoration(getResources()
                    .getDimensionPixelSize(R.dimen.grid_spacing_item_size), false));
        } else {
            layoutManager = new GridLayoutManager(getActivity(), 2);
            mNewsRecylerView.addItemDecoration(new SpaceItemDecoration(getResources()
                    .getDimensionPixelSize(R.dimen.grid_spacing_item_size), true));
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                @Override
                public int getSpanSize(int position) {
                    if(mNewsAdapter.getData().get(position) != null) {
                        return 1;
                    }
                    return 2;
                }
            });
        }
        mNewsRecylerView.setLayoutManager(layoutManager);
        mNewsRecylerView.setHasFixedSize(true);

        mSwipeRefreshLayout.setScrollUpChild(mNewsRecylerView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadNews(mChannel, true);
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            mChannel = args.getString(CHANNEL_ARGUMENT_KEY);
        }

        mNewsAdapter = new NewsAdapter(new ArrayList<News>(0), new NewsAdapter.NewsItemListener() {
            @Override
            public void onNewsClicked(News news) {
                mPresenter.openNewsDetail(news);
            }
        });
        mNewsRecylerView.setAdapter(mNewsAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.loadNews(mChannel, false);
    }

    @Override
    public void setRefreshIndicator(boolean active) {
        mSwipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showNetworkNotAvailable() {
        Snackbar.make(rootLayout, "Network not available!", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showNews(List<News> bunchOfNews) {
        if (bunchOfNews != null && !bunchOfNews.isEmpty()) {
            mNewsAdapter.replaceData(bunchOfNews);
        }
    }

    @Override
    public void showLoadingNewsError() {
        Toast.makeText(getActivity(), "Get news error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(NewsContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public NewsContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void showNewsDetailUi(String link) {
        Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
        intent.putExtra("link", link);
        startActivity(intent);
    }
}
