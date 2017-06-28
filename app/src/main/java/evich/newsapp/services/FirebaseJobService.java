package evich.newsapp.services;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import evich.newsapp.NewsApplication;
import evich.newsapp.dagger.component.ApplicationComponent;
import evich.newsapp.data.News;
import evich.newsapp.data.source.NewspaperRepository;
import evich.newsapp.data.source.remote.NewsApi;
import evich.newsapp.helper.NewspaperHelper;

/**
 * Created by Nhat on 6/8/2017.
 */

public class FirebaseJobService extends JobService {

    public static final String TAG = FirebaseJobService.class.getSimpleName();
    public static final String UPDATE_NEWS = "update_news";
    public static final String FAILED_CHANNELS_KEY = "failed_channels";

    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    // A queue of Runnables
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // Creates a thread pool manager
    private ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,       // Initial pool size
            NUMBER_OF_CORES,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mDecodeWorkQueue);


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FirebaseJobService onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "FirebaseJobService onDestroy");
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (jobParameters.getTag().equals(UPDATE_NEWS)) {
            doUpdateNews(jobParameters);
        }
        return true;
    }

    private void doUpdateNews(final JobParameters jobParameters) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                NewsApi newsApi = getApplicationComponent().getNewsApi();
                List<String> channels = jobParameters.getExtras()
                        .getStringArrayList(FAILED_CHANNELS_KEY);
                if (channels == null || channels.isEmpty()) {
                    channels = Arrays.asList(NewspaperHelper.getNewsChannels());
                }
                ArrayList<String> failedChannels =
                        new ArrayList<>(NewspaperHelper.NUM_OF_CHANNELS);
                for (String channel : channels) {
                    List<News> bunchOfNews = fetchNewsFromRemote(channel, newsApi);
                    if (bunchOfNews != null && !bunchOfNews.isEmpty()) {
                        updateRepository(bunchOfNews);
                    } else {
                        failedChannels.add(channel);
                    }
                }
                boolean shouldReschedule = false;
                if(!failedChannels.isEmpty()) {
                    shouldReschedule = true;
                }
                jobParameters.getExtras().putStringArrayList(FAILED_CHANNELS_KEY, failedChannels);
                jobFinished(jobParameters, shouldReschedule);
            }
        });
    }

    private List<News> fetchNewsFromRemote(String channel, NewsApi newsApi) {
        try {
            return newsApi.getNews(NewspaperHelper.getTypeChannel(channel),
                    String.valueOf(System.currentTimeMillis()), NewspaperRepository.MAX_NEWS)
                    .execute()
                    .body();
        } catch (IOException e) {
            Log.e(TAG, "Failed to GET " + channel, e);
        }
        return null;
    }

    private void updateRepository(final List<News> bunchOfNews) {
        Log.d(TAG, "GET: " + bunchOfNews.get(0).getChannelTitle() + "-" + bunchOfNews.size());
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                getApplicationComponent()
                        .getNewspaperRepository()
                        .saveBunchOfNews(bunchOfNews);
            }
        });
    }

    private ApplicationComponent getApplicationComponent() {
        return ((NewsApplication) getApplication())
                .getApplicationComponent();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
