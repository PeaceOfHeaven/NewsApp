package evich.newsapp.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import evich.newsapp.data.News;
import evich.newsapp.data.source.local.NewspaperLocalDataSource;
import evich.newsapp.data.source.remote.NewspaperRemoteDataSource;
import evich.newsapp.helper.NewspaperHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 14/05/2016.
 */
public class NewspaperRepository implements NewspaperDataSource {

    private static NewspaperRepository mNewspaperRepository;
    private NewspaperRemoteDataSource mNewspaperRemoteDataSource;
    private NewspaperLocalDataSource mNewspaperLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Map<String, News>> mBunchOfNewsByChannel;

    private SparseIntArray mChannelNeedChanged;

    private NewspaperRepository(NewspaperRemoteDataSource newspaperRemoteDataSource,
                                NewspaperLocalDataSource newspaperLocalDataSource) {
        mNewspaperRemoteDataSource = newspaperRemoteDataSource;
        mNewspaperLocalDataSource = newspaperLocalDataSource;

        mChannelNeedChanged = new SparseIntArray();
    }

    public static NewspaperRepository getInstance(NewspaperRemoteDataSource
                                                          newspaperRemoteDataSource,
                                                  NewspaperLocalDataSource
                                                          newspaperLocalDataSource) {
        if (mNewspaperRepository == null) {
            mNewspaperRepository = new NewspaperRepository(newspaperRemoteDataSource,
                    newspaperLocalDataSource);
        }
        return mNewspaperRepository;
    }

    public static void destroyInstance() {
        mNewspaperRepository = null;
    }

    private List<NewsRepositoryObserver> mObservers = new
            ArrayList<NewsRepositoryObserver>();

    public void addContentObserver(NewsRepositoryObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public void removeContentObserver(NewsRepositoryObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    private void notifyContentObserver(String channel, int newsRetrieveParam) {
        for (NewsRepositoryObserver observer : mObservers) {
            if (observer.onNewsNeedChanged(channel, newsRetrieveParam) == true) {
                break;
            }
        }
    }

    @Nullable
    @Override
    public List<News> getNewsByChannel(String channel) {
        checkNotNull(channel, "channel title cannot be null");

        List<News> bunchOfNews = null;

        if (cachedNewsByChannelAvailable(channel)) {
            bunchOfNews = getCachedNewsByChannel(channel);
        } else {
            bunchOfNews = mNewspaperLocalDataSource.getNewsByChannel(channel);
        }

        int channelType = NewspaperHelper.getTypeChannel(channel);
        int newsRetrieveParams = mChannelNeedChanged.get(channelType);

        if (bunchOfNews == null || bunchOfNews.isEmpty() || newsRetrieveParams ==
                NewsRetrieveParams.FORCE_REFRESH) {
            bunchOfNews = mNewspaperRemoteDataSource.getNewsByChannel(channel);
            saveNewsInLocalDataSource(bunchOfNews);
            bunchOfNews = mNewspaperLocalDataSource.getNewsByChannel(channel);

        } else if (newsRetrieveParams == NewsRetrieveParams.FORCE_LOAD_MORE) {
            long time = Long.valueOf(bunchOfNews.get(bunchOfNews.size() - 1).getPubdate());
            bunchOfNews = mNewspaperRemoteDataSource.loadMoreNewsByChannel(channel, time);
            saveNewsInLocalDataSource(bunchOfNews);
        }
        mChannelNeedChanged.put(channelType, NewsRetrieveParams.NONE);
        processLoadedNews(channel, bunchOfNews);

        return !mBunchOfNewsByChannel.containsKey(channel) ?
                null : new ArrayList<>(mBunchOfNewsByChannel.get
                (channel).values());
    }

    private void saveNewsInLocalDataSource(List<News> bunchOfNews) {
        if (bunchOfNews != null && !bunchOfNews.isEmpty()) {
            for (News news : bunchOfNews) {
                mNewspaperLocalDataSource.saveSingleNews(news);
            }
        }
    }

    private void processLoadedNews(String channel, List<News> bunchOfNews) {
        if (bunchOfNews == null) {
            return;
        }
        if (mBunchOfNewsByChannel == null) {
            mBunchOfNewsByChannel = new LinkedHashMap<>();
        }

        Map<String, News> bunchOfCachedNews = mBunchOfNewsByChannel.get(channel);
        if (bunchOfCachedNews == null) {
            bunchOfCachedNews = new LinkedHashMap<>();
        }
        for (int i = 0; i < bunchOfNews.size(); i++) {
            News news = bunchOfNews.get(i);
            bunchOfCachedNews.put(news.getId(), news);
        }
        mBunchOfNewsByChannel.put(channel, bunchOfCachedNews);
    }

    public boolean cachedNewsByChannelAvailable(String channel) {
        return mBunchOfNewsByChannel != null && mBunchOfNewsByChannel.containsKey(channel);
    }

    public List<News> getCachedNewsByChannel(String channel) {
        return mBunchOfNewsByChannel == null && !mBunchOfNewsByChannel.containsKey(channel) ?
                null : new ArrayList<>(mBunchOfNewsByChannel.get
                (channel).values());
    }

    @Override
    public void saveSingleNews(@NonNull News news) {
        mNewspaperLocalDataSource.saveSingleNews(news);

        if (mBunchOfNewsByChannel == null) {
            mBunchOfNewsByChannel = new LinkedHashMap<>();
        }
        Map<String, News> bunchOfCachedNews = mBunchOfNewsByChannel.get(NewspaperHelper
                .getChannel(news.getChannel()));
        if (bunchOfCachedNews == null) {
            bunchOfCachedNews = new LinkedHashMap<>();
        }
        bunchOfCachedNews.put(news.getId(), news);
    }

    @Override
    public void refreshNews(String channel, int newsRetrieveParam) {
        mChannelNeedChanged.put(NewspaperHelper.getTypeChannel(channel), newsRetrieveParam);
        notifyContentObserver(channel, newsRetrieveParam);
    }

    @Override
    public void deleteAllNewsByChannel(String channel) {

    }

    @Override
    public void clearCachedNews() {
        mNewspaperLocalDataSource.clearCachedNews();
        if(mBunchOfNewsByChannel != null) {
            mBunchOfNewsByChannel.clear();
        }
    }

    public interface NewsRepositoryObserver {

        boolean onNewsNeedChanged(String channel, int retrieveParam);
    }
}
