package evich.newsapp.news;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import evich.newsapp.dagger.extras.ActivityContext;
import evich.newsapp.data.News;
import evich.newsapp.data.source.LoaderResult;
import evich.newsapp.data.source.NewsLoader;
import evich.newsapp.data.source.NewspaperRepository;
import evich.newsapp.helper.NetworkHelper;
import evich.newsapp.helper.NewspaperHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 14/05/2016.
 */
public class NewsPresenter implements NewsContract.Presenter, LoaderManager
        .LoaderCallbacks<LoaderResult>, NewspaperRepository.NewsRepositoryObserver {

    private static final String TAG = NewsPresenter.class.getSimpleName();
    private final int NEWS_LOADER_ID = 333;
    private static final String CHANNEL_BUNDLE_KEY = "channel";

    private Context mContext;
    private NewspaperRepository mNewspaperRepository;
    private LoaderManager mLoaderManager;
    private ArrayList<String> mRequestChannels;
    private SimpleArrayMap<String, NewsContract.View> mViewsByChannel;
    private NewsLoader mNewsLoader;
    private Handler mHandler = new Handler();

    @Inject
    public NewsPresenter(@ActivityContext Context context,
                         @NonNull LoaderManager loaderManager,
                         @NonNull NewspaperRepository newspaperRepository) {
        mContext = checkNotNull(context, "context cannot be null");
        mLoaderManager = checkNotNull(loaderManager, "loader manager cannot be null");
        mNewspaperRepository = checkNotNull(newspaperRepository, "newspaperRepository cannot be " +
                "null");
        mViewsByChannel = new SimpleArrayMap<>(NewspaperHelper.NUM_OF_CHANNELS);
    }

    @Override
    public void attachViewByChannel(@NonNull String channel, @NonNull NewsContract.View view) {
        checkNotNull(channel);
        checkNotNull(view);

        view.setPresenter(this);
        mViewsByChannel.put(channel, view);
    }

    @Override
    public void detachViewByChannel(@NonNull String channel) {
        checkNotNull(channel);
        mViewsByChannel.remove(channel);
    }

    @Override
    public void loadNews(String channel, boolean refresh) {
        NewsContract.View view = mViewsByChannel.get(channel);
        if (refresh) {
            if(view != null) {
                if (!NetworkHelper.isOnline(mContext)) {
                    view.showNetworkNotAvailable();
                }
                view.setRefreshIndicator(true);
            }
            mNewspaperRepository.refreshNews(channel);
            return;
        }
        queueRequestChannel(channel);
        processRequestChannels();
    }

    private void queueRequestChannel(String channel) {
        if (mRequestChannels == null) {
            mRequestChannels = new ArrayList<>();
        }
        mRequestChannels.remove(channel);
        mRequestChannels.add(channel);
    }

    private void processRequestChannels() {
        if (mRequestChannels.size() > 0) {
            String channel = mRequestChannels.get(mRequestChannels.size() - 1);

            if (mNewsLoader == null) {
                Bundle bundle = new Bundle();
                bundle.putString(CHANNEL_BUNDLE_KEY, channel);
                mLoaderManager.initLoader(NEWS_LOADER_ID, bundle, this);
            } else if (!mNewsLoader.isLoading() && mNewsLoader.isStarted()) {
                mNewsLoader.setCurrentChannel(channel);
                mNewsLoader.forceLoad();
            }
        }
    }

    @Override
    public void start() {
        mNewspaperRepository.addContentObserver(this);
    }

    @Override
    public void finish() {
        mViewsByChannel.clear();
        mNewspaperRepository.removeContentObserver(this);
    }

    @Override
    public void openNewsDetail(News news) {
        if (NetworkHelper.isOnline(mContext)) {
            mViewsByChannel.get(news.getChannelTitle()).showNewsDetailUi(news.getLink());
        } else {
            mViewsByChannel.get(news.getChannelTitle()).showNetworkNotAvailable();
        }
    }

    @Override
    public Loader<LoaderResult> onCreateLoader(int id, Bundle args) {
        if (args.containsKey(CHANNEL_BUNDLE_KEY)) {
            String channel = args.getString(CHANNEL_BUNDLE_KEY);

            NewsContract.View view = mViewsByChannel.get(channel);
            if (view != null) {
                view.setRefreshIndicator(true);
            }

            if (mNewsLoader == null) {
                mNewsLoader = new NewsLoader(mContext, mNewspaperRepository, channel);
            }
            return mNewsLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult data) {
        NewsLoader.NewsLoaderResult newsLoaderResult = (NewsLoader.NewsLoaderResult) data.getResult();
        final List<News> bunchOfNews = newsLoaderResult.getBunchOfNews();

        String channel = newsLoaderResult.getChannel();
        mRequestChannels.remove(channel);
        Log.d(TAG, "Loaded " + channel + ": " + bunchOfNews.size());

        final NewsContract.View view = mViewsByChannel.get(channel);
        if (view != null) {
            if (bunchOfNews == null) {
                view.showLoadingNewsError();
            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setRefreshIndicator(false);
                    }
                }, 300);
                view.showNews(bunchOfNews);
            }
        }
        processRequestChannels();
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult> loader) {
        Log.d(TAG, "onLoaderReset called");
    }

    @Override
    public void onUpdatedNews(final String channel) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mViewsByChannel.containsKey(channel)) {
                    queueRequestChannel(channel);
                    processRequestChannels();
                }
            }
        });

    }
}
