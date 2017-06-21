package evich.newsapp.data.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import evich.newsapp.dagger.extras.Local;
import evich.newsapp.dagger.extras.Remote;
import evich.newsapp.data.News;
import evich.newsapp.helper.NewspaperHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 14/05/2016.
 */
@Singleton
public class NewspaperRepository implements NewspaperDataSource {

    public static final int MAX_NEWS = 30;

    private final NewspaperDataSource mNewspaperRemoteDataSource;
    private final NewspaperDataSource mNewspaperLocalDataSource;

    private Map<String, List<News>> mBunchOfNewsByChannel;

    private static final ArrayList<String> mSupportedChannels = new ArrayList<>();
    static {
        mSupportedChannels.addAll(Arrays.asList(NewspaperHelper.getNewsChannels()));
    }

    @Inject
    public NewspaperRepository(@Remote NewspaperDataSource newspaperRemoteDataSource,
                               @Local NewspaperDataSource newspaperLocalDataSource) {
        checkNotNull(newspaperLocalDataSource);
        checkNotNull(newspaperRemoteDataSource);

        mNewspaperRemoteDataSource = newspaperRemoteDataSource;
        mNewspaperLocalDataSource = newspaperLocalDataSource;
    }

    private List<NewsRepositoryObserver> mObservers = new
            ArrayList<NewsRepositoryObserver>();

    public void addContentObserver(NewsRepositoryObserver observer) {
        checkNotNull(observer);
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public void removeContentObserver(NewsRepositoryObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    private void notifyContentObserver(String channel) {
        for (NewsRepositoryObserver observer : mObservers) {
            observer.onUpdatedNews(channel);
        }
    }

    @Override
    public List<News> getNews(String channel) {
        if(!mSupportedChannels.contains(channel)) {
            throw new IllegalArgumentException(channel + " not supported!");
        }
        List<News> bunchOfNews;

        if (cachedNewsAvailable(channel)) {
            return getCachedNews(channel);
        } else {
            bunchOfNews = mNewspaperLocalDataSource.getNews(channel);
        }

        if (bunchOfNews == null || bunchOfNews.isEmpty()) {
            getNewsFromRemoteAndSaveLocal(channel);
            bunchOfNews = mNewspaperLocalDataSource.getNews(channel);
        }
        processLoadedNews(channel, bunchOfNews);
        return mBunchOfNewsByChannel.get(channel);
    }

    private void getNewsFromRemoteAndSaveLocal(String channel) {
        List<News> bunchOfNews = mNewspaperRemoteDataSource.getNews(channel);
        mNewspaperLocalDataSource.saveBunchOfNews(bunchOfNews);
    }

    private void processLoadedNews(String channel, List<News> bunchOfNews) {
        if (mBunchOfNewsByChannel == null) {
            mBunchOfNewsByChannel = new LinkedHashMap<>();
        }

        List<News> bunchOfCachedNews = mBunchOfNewsByChannel.get(channel);
        if (bunchOfCachedNews == null) {
            bunchOfCachedNews = new ArrayList<>();
        }
        bunchOfCachedNews.clear();
        for (News news : bunchOfNews) {
            bunchOfCachedNews.add(news);
        }
        mBunchOfNewsByChannel.put(channel, bunchOfCachedNews);
    }

    public boolean cachedNewsAvailable(String channel) {
        return mBunchOfNewsByChannel != null && mBunchOfNewsByChannel.containsKey(channel);
    }

    public List<News> getCachedNews(String channel) {
        if(!mSupportedChannels.contains(channel)) {
            throw new IllegalArgumentException(channel + " not supported!");
        }
        return mBunchOfNewsByChannel.get(channel);
    }

    @Override
    public boolean saveBunchOfNews(List<News> bunchOfNews) {
        checkNotNull(bunchOfNews);

        if(mNewspaperLocalDataSource.saveBunchOfNews(bunchOfNews)) {
            if (mBunchOfNewsByChannel == null) {
                mBunchOfNewsByChannel = new LinkedHashMap<>();
            }
            String channel = bunchOfNews.get(0).getChannelTitle();
            processLoadedNews(channel, mNewspaperLocalDataSource.getNews(channel));
            notifyContentObserver(channel);
            return true;
        }
        return false;
    }

    @Override
    public void refreshNews(String channel) {
        if(!mSupportedChannels.contains(channel)) {
            throw new IllegalArgumentException(channel + " not supported!");
        }
        notifyContentObserver(channel);
    }


    public interface NewsRepositoryObserver {

        void onUpdatedNews(String channel);
    }
}
