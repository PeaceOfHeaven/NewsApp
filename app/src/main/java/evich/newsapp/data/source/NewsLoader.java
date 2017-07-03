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
public class NewsLoader extends AsyncTaskLoader<LoaderResult> {

    private final NewspaperRepository mNewspaperRepository;

    private String mCurrentChannel;
    private boolean isLoading = false;

    public NewsLoader(Context context, @NonNull NewspaperRepository newspaperRepository, String currentChannel) {
        super(context);
        mNewspaperRepository = checkNotNull(newspaperRepository, "newspaperRepository cannot be " +
                "null");
        mCurrentChannel = checkNotNull(currentChannel);
    }

    public void setCurrentChannel(String currentChannel) {
        mCurrentChannel = currentChannel;
    }

    @Override
    protected void onStartLoading() {
        boolean isCachedAvailable = mNewspaperRepository.cachedNewsAvailable(mCurrentChannel);
        if (isCachedAvailable) {
            deliverResult(new NewsLoaderResult(mCurrentChannel,
                    mNewspaperRepository.getCachedNews(mCurrentChannel)));
        }

        if (takeContentChanged() || !isCachedAvailable) {
            // When a change has  been delivered or the repository cache isn't available, we force
            // a load.
            forceLoad();
        }
    }

    @Override
    public LoaderResult loadInBackground() {
        isLoading = true;
        return new NewsLoaderResult(mCurrentChannel, mNewspaperRepository.getNews(mCurrentChannel));
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public void deliverResult(LoaderResult result) {
        isLoading = false;

        if (isReset()) {
            return;
        }

        if (isStarted()) {
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStopLoading() {
        isLoading = false;
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
    }

    public final class NewsLoaderResult implements LoaderResult {

        private String mChannel;
        private List<News> mBunchOfNews;

        private NewsLoaderResult(String channel, List<News> result) {
            mChannel = channel;
            mBunchOfNews = result;
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
    }
}
