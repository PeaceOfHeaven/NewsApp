package evich.newsapp.news;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import evich.newsapp.data.News;
import evich.newsapp.data.source.LoaderResult;
import evich.newsapp.data.source.NewsLoader;
import evich.newsapp.data.source.NewsRetrieveParams;
import evich.newsapp.data.source.NewspaperRepository;
import evich.newsapp.helper.NewspaperHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 14/05/2016.
 */
public class NewsPresenter implements NewsContract.Presenter, LoaderManager
        .LoaderCallbacks<LoaderResult> {

    private final int LOADER_INIT_ID = 333;

    private final int MAX_LOADER_COUNT = 3;

    private final String CHANNEL_BUNDLE_KEY = "channel";

    private Context mContext;

    private NewspaperRepository mNewspaperRepository;

    private final LoaderManager mLoaderManager;

    private Map<String, List<News>> mCurrentBunchOfNews;

    private SparseBooleanArray mLoadersRunning;

    private ArrayList<String> mRequestChannels;

    private SparseArrayCompat<NewsContract.View> mViewsByChannel;

    public NewsPresenter(Context context, @NonNull LoaderManager loaderManager, @NonNull
    NewspaperRepository

            newspaperRepository) {
        mContext = context;

        mLoaderManager = checkNotNull(loaderManager, "loader manager cannot be null");
        mNewspaperRepository = checkNotNull(newspaperRepository, "newspaperRepository cannot be " +
                "null");
        mLoadersRunning = new SparseBooleanArray(0);
        mViewsByChannel = new SparseArrayCompat<>(0);
    }

    @Override
    public void attachViewByChannel(@NonNull String channel, @NonNull NewsContract.View view) {
        checkNotNull(channel);
        checkNotNull(view);

        view.setPresenter(this);
        mViewsByChannel.put(NewspaperHelper.getTypeChannel(channel), view);
    }

    @Override
    public void detachViewByChannel(@NonNull String channel) {
        checkNotNull(channel);

        mViewsByChannel.remove(NewspaperHelper.getTypeChannel(channel));
    }

    @Override
    public void loadNewsByChannel(String channel, int newsRetrieveParams) {
        int channelType = NewspaperHelper.getTypeChannel(channel);

        if (mCurrentBunchOfNews == null) {
            mCurrentBunchOfNews = new LinkedHashMap<>();
        }

        NewsContract.View view = mViewsByChannel.get(channelType);
        if(newsRetrieveParams != NewsRetrieveParams.NONE) {
            int newsRefreshParam = newsRetrieveParams;
            if(newsRefreshParam == NewsRetrieveParams.FORCE_LOAD_MORE) {
                view.setLoadingMoreIndicator(true);
            } else {
                //view.setRefreshIndicator(true);
            }
            mNewspaperRepository.refreshNews(channel, newsRefreshParam);
            return;
        }

        if (mCurrentBunchOfNews.containsKey(channel)) {
            view.showNews(mCurrentBunchOfNews.get(channel));
            return;
        }

        if (mRequestChannels == null) {
            mRequestChannels = new ArrayList<>();
        }
        mRequestChannels.remove(channel);
        mRequestChannels.add(channel);

        Bundle bundle = new Bundle();
        bundle.putString(CHANNEL_BUNDLE_KEY, channel);

        int loaderId = getFreeLoader();
        if(loaderId != -1) {
            processNewsLoader(loaderId, bundle);
        }
    }

    private int getFreeLoader() {
        int loaderId = -1;
        if (mLoadersRunning.size() < MAX_LOADER_COUNT) {
            loaderId = LOADER_INIT_ID + mLoadersRunning.size();
        } else {
            for(int i = 0; i < MAX_LOADER_COUNT; i++) {
                int id = LOADER_INIT_ID + i;
                Loader loader = mLoaderManager.getLoader(loaderId);
                if(loader != null && loader instanceof NewsLoader) {
                    NewsLoader newsLoader = (NewsLoader) loader;
                    if (!newsLoader.isLoading()) {
                        loaderId = id;
                        break;
                    }
                }
            }
        }
        return loaderId;
    }

    private void processNewsLoader(final int loaderId, final Bundle bundle) {
        if (mLoaderManager.getLoader(loaderId) == null) {
            mLoaderManager.initLoader(loaderId, bundle, this);
        } else {
            mLoaderManager.restartLoader(loaderId, bundle, this);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void finish() {
        mNewspaperRepository.clearCachedNews();
    }

    @Override
    public void openNewsDetail(News requestedNews) {
        checkNotNull(requestedNews, "requestedNews cannot be null!");
        mViewsByChannel.get(requestedNews.getChannel()).showNewsDetailUi(requestedNews.getLink());
    }

    @Override
    public Loader<LoaderResult> onCreateLoader(int id, Bundle args) {
        if (args.containsKey(CHANNEL_BUNDLE_KEY)) {
            String channel = args.getString(CHANNEL_BUNDLE_KEY);
            NewsContract.View view = mViewsByChannel.get(NewspaperHelper.getTypeChannel(channel));
            if(view != null) {
                view.setLoadingIndicator(true);
            }
            mRequestChannels.remove(channel);
            mLoadersRunning.put(id, true);
            return new NewsLoader(mContext, mNewspaperRepository, channel);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult data) {
        final int id = loader.getId();
        mLoadersRunning.put(id, false);

        NewsLoader.NewsLoaderResult newsLoaderResult = (NewsLoader.NewsLoaderResult) data.getResult();

        String channel = newsLoaderResult.getChannel();
        List<News> bunchOfNews = newsLoaderResult.getBunchOfNews();
        int newsRetrieveParam = newsLoaderResult.getNewsRetrieveParams();

        NewsContract.View view = mViewsByChannel.get(NewspaperHelper.getTypeChannel(channel));

        if (bunchOfNews == null) {
            //use View to show errors
            if(view != null) {
                view.showLoadingNewsError();
            }
        } else {
            // show new data
            boolean dataHasChanged = true;
            List<News> oldData = mCurrentBunchOfNews.get(channel);
            if(oldData == null) {
                mCurrentBunchOfNews.put(channel, bunchOfNews);
            } else {
                dataHasChanged = oldData.size() == bunchOfNews.size() ? false : true;
            }

            if(view != null && dataHasChanged) {
                switch (newsRetrieveParam) {
                    case NewsRetrieveParams.NONE:
                        view.setLoadingIndicator(false);
                        break;
                    case NewsRetrieveParams.FORCE_LOAD_MORE:
                        view.setLoadingMoreIndicator(false);
                        break;
                    case NewsRetrieveParams.FORCE_REFRESH:
                        view.setRefreshIndicator(false);
                        break;
                }
                view.showNews(bunchOfNews);
            }
        }

        if (mRequestChannels.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putString(CHANNEL_BUNDLE_KEY, mRequestChannels.get(mRequestChannels.size() - 1));
            processNewsLoader(id, bundle);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult> loader) {
        Log.d("Newspaper", "onLoaderReset called");
    }
}
