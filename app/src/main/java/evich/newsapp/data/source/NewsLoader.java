package evich.newsapp.data.source;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import evich.newsapp.data.News;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 17/05/2016.
 */
public class NewsLoader extends AsyncTaskLoader<LoaderResult> implements
        NewspaperRepository.NewsRepositoryObserver {

    private final NewspaperRepository mNewspaperRepository;

    private String mChannel;
    private boolean isLoading = false;
    private int mCurrentNewsRetrieveParam;

    public NewsLoader(Context context, @NonNull NewspaperRepository newspaperRepository, String channel) {
        super(context);
        mNewspaperRepository = checkNotNull(newspaperRepository, "newspaperRepository cannot be " +
                "null");

        mChannel = checkNotNull(channel);
        mCurrentNewsRetrieveParam = NewsRetrieveParams.NONE;
    }

    @Override
    protected void onStartLoading() {
        if (mNewspaperRepository.cachedNewsByChannelAvailable(mChannel)) {
            deliverResult(new NewsLoaderResult(mChannel, mCurrentNewsRetrieveParam, mNewspaperRepository.getCachedNewsByChannel(mChannel)));
        }

        // Begin monitoring the underlying data source.
        mNewspaperRepository.addContentObserver(this);

        if (takeContentChanged() || !mNewspaperRepository.cachedNewsByChannelAvailable(mChannel)) {
            // When a change has  been delivered or the repository cache isn't available, we force
            // a load.
            forceLoad();
        }
    }

    @Override
    public LoaderResult loadInBackground() {
        isLoading = true;
        return new NewsLoaderResult(mChannel, mCurrentNewsRetrieveParam, mNewspaperRepository.getNewsByChannel(mChannel));
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public void deliverResult(LoaderResult result) {
        setLoaderLoaded();

        if (isReset()) {
            return;
        }

        if (isStarted()) {
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStopLoading() {
        setLoaderLoaded();
        cancelLoad();
    }

    private void setLoaderLoaded() {
        isLoading = false;
        mCurrentNewsRetrieveParam = NewsRetrieveParams.NONE;
    }

    @Override
    protected void onReset() {
        onStopLoading();
        mNewspaperRepository.removeContentObserver(this);
    }

    @Override
    public boolean onNewsNeedChanged(String channel, int newsRetrieveParam) {
        if (isStarted() && !isLoading) {
            mChannel = channel;
            mCurrentNewsRetrieveParam = newsRetrieveParam;
            forceLoad();
            return true;
        }
        return false;
    }

    public final class NewsLoaderResult implements LoaderResult {

        private String mChannel;
        private List<News> mBunchOfNews;
        private int mNewsRetrieveParam;

        private NewsLoaderResult(String channel, int newsRetrieveParam, List<News> result) {
            mChannel = channel;
            mBunchOfNews = result;
            mNewsRetrieveParam = newsRetrieveParam;
        }

        @Override
        public NewsLoaderResult getResult() {
            return this;
        }

        public String getChannel() {
            return mChannel;
        }

        public List<News> getBunchOfNews() {
            return mBunchOfNews;
        }

        public int getNewsRetrieveParams() {
            return mNewsRetrieveParam;
        }
    }
}
