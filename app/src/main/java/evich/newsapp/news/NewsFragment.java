package evich.newsapp.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
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

import evich.newsapp.R;
import evich.newsapp.data.News;
import evich.newsapp.data.source.NewsRetrieveParams;
import evich.newsapp.helper.NetworkHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 18/05/2016.
 */
public class NewsFragment extends Fragment implements NewsContract.View {

    public static final String CHANNEL_ARGUMENT_KEY = "channel";

    private NewsContract.Presenter mPresenter;

    private LoadMoreRecylerView mNewsRecylerView;
    private NewsAdapter mNewsAdapter;
    private ScrollChildSwipeRefreshLayout mSwipeRefreshLayout;

    private List<News> mBunchOfNews;
    private String mChannel;

    private FrameLayout loadingLayout;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("News", "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("News", "onDetach");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        mSwipeRefreshLayout = (ScrollChildSwipeRefreshLayout) view
                .findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));

        loadingLayout = (FrameLayout) view.findViewById(R.id.loadingLayout);

        mNewsRecylerView = (LoadMoreRecylerView) view.findViewById(R.id.news_recyclerView);

        boolean screenSmall = getResources().getBoolean(R.bool.screen_small);

        RecyclerView.LayoutManager layoutManager = null;
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
        mNewsRecylerView.setOnLoadMoreListener(new LoadMoreRecylerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mPresenter.loadNewsByChannel(mChannel, NewsRetrieveParams.FORCE_LOAD_MORE);
            }
        });

        mSwipeRefreshLayout.setScrollUpChild(mNewsRecylerView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadNewsByChannel(mChannel, NewsRetrieveParams.FORCE_REFRESH);
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            mChannel = args.getString(CHANNEL_ARGUMENT_KEY);
        }
        if (mBunchOfNews == null) {
            mBunchOfNews = new ArrayList<>();
        }

        mNewsAdapter = new NewsAdapter(mBunchOfNews, mNewsItemListener);
        mNewsRecylerView.setAdapter(mNewsAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NetworkHelper.isOnline(getActivity())) {
            mPresenter.loadNewsByChannel(mChannel, NewsRetrieveParams.NONE);
        }
    }

    private NewsAdapter.NewsItemListener mNewsItemListener = new NewsAdapter.NewsItemListener() {

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
    public void setLoadingMoreIndicator(boolean active) {
        if (active) {
            mBunchOfNews.add(null);
            mNewsAdapter.notifyItemInserted(mBunchOfNews.size() - 1);
        } else {
            mBunchOfNews.remove(mBunchOfNews.size() - 1);
            mNewsAdapter.notifyItemRemoved(mBunchOfNews.size() - 1);
        }
    }

    @Override
    public void setRefreshIndicator(boolean active) {
        mSwipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showNews(List<News> bunchOfNews) {
        if (bunchOfNews != null) {
            mBunchOfNews = bunchOfNews;
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

    private static class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
            RecyclerView.ViewHolder holder = null;

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
                    Picasso.with(newsViewHolder.itemView.getContext()).load(news.getImgUrl())
                            .into(newsViewHolder.newsImageImgView);
                } else {
                    newsViewHolder.newsImageImgView.setImageBitmap(null);
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
            mBunchOfNews = bunchOfNews;
            notifyDataSetChanged();
        }

        public List<News> getData() {
            return mBunchOfNews;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView newsImageImgView;
            public TextView newsTitleTxtView;
            public TextView newsPubdateTxtView;

            public ViewHolder(View itemView) {
                super(itemView);

                newsImageImgView = (ImageView) itemView.findViewById(R.id.newsImg_imgView);
                newsTitleTxtView = (TextView) itemView.findViewById(R.id
                        .newsTitle_txtView);
                newsPubdateTxtView = (TextView) itemView.findViewById(R.id.newsPublicDate_txtView);
            }
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {

            public ProgressBar progressBar;

            public LoadingViewHolder(View itemView) {
                super(itemView);
                progressBar = (ProgressBar) itemView.findViewById(R.id.loadingMoreProgressBar);
            }
        }

        public interface NewsItemListener {
            void onNewsClicked(News news);
        }
    }
}
