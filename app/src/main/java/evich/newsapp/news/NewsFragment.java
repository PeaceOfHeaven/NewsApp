package evich.newsapp.news;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

    @BindView(R.id.loadingLayout)
    FrameLayout loadingLayout;

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
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
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

        mNewsAdapter = new NewsAdapter(new ArrayList<News>(), mNewsItemListener);
        mNewsRecylerView.setAdapter(mNewsAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.loadNews(mChannel, false);
    }

    private NewsItemListener mNewsItemListener = new NewsItemListener() {

        @Override
        public void onNewsClicked(News news) {
            mPresenter.openNewsDetail(news);
        }
    };

    @Override
    public void setLoadingIndicator(boolean active) {
        loadingLayout.setVisibility(active == true ? View.VISIBLE : View.INVISIBLE);
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
        if (bunchOfNews != null) {
            mNewsAdapter.replaceData(bunchOfNews);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNewsRecylerView.setLoaded();
                }
            }, 1000);
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

    class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        private List<News> mBunchOfNews;
        private final NewsItemListener mNewspaperItemListener;

        public NewsAdapter(final List<News> bunchOfNews, final NewsItemListener
                newspaperItemListener) {
            mBunchOfNews = bunchOfNews;
            mNewspaperItemListener = newspaperItemListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            RecyclerView.ViewHolder holder;

            if (viewType == VIEW_TYPE_ITEM) {
                View view = inflater.inflate(R.layout.news_item, parent, false);
                holder = new ViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.news_loading_item, parent, false);
                holder = new LoadingViewHolder(view);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final News news = mBunchOfNews.get(position);

            if (holder instanceof ViewHolder) {
                ViewHolder newsViewHolder = (ViewHolder) holder;
                if (!TextUtils.isEmpty(news.getImgUrl())) {
                    Picasso.with(getActivity()).load(news.getImgUrl())
                            .into(newsViewHolder.newsImageImgView);
                    newsViewHolder.newsImageImgView.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    Picasso.with(getActivity()).load(android.R.color.transparent)
                            .into(newsViewHolder.newsImageImgView);
                    newsViewHolder.newsImageImgView.setBackgroundColor(Color.TRANSPARENT);
                }
                newsViewHolder.newsTitleTxtView.setText(news.getTitle());

                String convertedDate = (String) DateUtils
                        .getRelativeDateTimeString(holder.itemView.getContext(), Long.parseLong
                                        (news.getPubdate()) * 1000,
                                DateUtils.SECOND_IN_MILLIS,
                                DateUtils.WEEK_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_TIME);
                ((ViewHolder) holder).newsPubdateTxtView.setText(convertedDate);

                newsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNewspaperItemListener.onNewsClicked(news);
                    }
                });
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return mBunchOfNews.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mBunchOfNews.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        public void replaceData(List<News> bunchOfNews) {
            final NewsDiffCallback diffCallback
                    = new NewsDiffCallback(mBunchOfNews, bunchOfNews);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            mBunchOfNews.clear();
            mBunchOfNews.addAll(bunchOfNews);
            diffResult.dispatchUpdatesTo(this);
        }

        public List<News> getData() {
            return mBunchOfNews;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.newsImg_imgView)
            public ImageView newsImageImgView;

            @BindView(R.id.newsTitle_txtView)
            public TextView newsTitleTxtView;

            @BindView(R.id.newsPublicDate_txtView)
            public TextView newsPubdateTxtView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.loadingMoreProgressBar)
            public ProgressBar progressBar;

            public LoadingViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    public interface NewsItemListener {
        void onNewsClicked(News news);
    }

    static class NewsDiffCallback extends DiffUtil.Callback {

        private final List<News> mOldNewsList;
        private final List<News> mNewNewsList;

        public NewsDiffCallback(List<News> oldNewsList, List<News> newNewsList) {
            mOldNewsList = oldNewsList;
            mNewNewsList = newNewsList;
        }

        @Override
        public int getOldListSize() {
            return mOldNewsList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewNewsList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldNewsList.get(oldItemPosition).getId()
                    .equals(mNewNewsList.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldNewsList.get(oldItemPosition).getId()
                    .equals(mNewNewsList.get(newItemPosition));
        }
    }
}
