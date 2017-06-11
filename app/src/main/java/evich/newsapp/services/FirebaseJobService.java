package evich.newsapp.services;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import evich.newsapp.NewsApplication;
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
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(TAG, "on start job: " + Thread.currentThread().getName());
        Log.d(TAG, "on start job: " + jobParameters.getTag());

        if (jobParameters.getTag().equals(UPDATE_NEWS)) {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Kickoff service");
                    NewsApi newsApi = ((NewsApplication) getApplication())
                            .getApplicationComponent().getNewsApi();
                    String[] channels = NewspaperHelper.getNewsChannels();
                    for (String channel : channels) {
                        Log.d(TAG, "GET: " + channel);
                        List<News> bunchOfNews;
                        try {
                            bunchOfNews = newsApi.getNews(NewspaperHelper.getTypeChannel(channel)+"",
                                    String.valueOf(System.currentTimeMillis()), NewspaperRepository.MAX_NEWS)
                                    .execute()
                                    .body();
                            Log.d(TAG, "GET: " + bunchOfNews.size());
                            storeBunchOfNews(bunchOfNews);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to GET " + channel, e);
                        }
                    }
                    jobFinished(jobParameters, false);
                }
            });
        }
        return true;
    }

    private void storeBunchOfNews(final List<News> bunchOfNews) {
        if(bunchOfNews != null && !bunchOfNews.isEmpty()) {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ((NewsApplication) getApplication())
                            .getApplicationComponent()
                            .getNewspaperRepository()
                            .saveBunchOfNews(bunchOfNews);
                }
            });
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
