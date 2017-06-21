package evich.newsapp.data.source.remote;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import evich.newsapp.data.News;
import evich.newsapp.data.source.NewspaperDataSource;
import evich.newsapp.data.source.NewspaperRepository;
import evich.newsapp.helper.NewspaperHelper;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 14/05/2016.
 */
@Singleton
public class NewspaperRemoteDataSource implements NewspaperDataSource {

    private static final String TAG = NewspaperRemoteDataSource.class.getSimpleName();
    private NewsApi mNewsApi;

    @Inject
    public NewspaperRemoteDataSource() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://quangtin.esy.es/docbao/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mNewsApi = retrofit.create(NewsApi.class);
    }

    @Nullable
    @Override
    public List<News> getNews(String channel) {
        checkNotNull(channel);
        return fetchNewsByChannelWithTime(channel, System.currentTimeMillis());
    }

    private List<News> fetchNewsByChannelWithTime(String channel, long timeInMillis) {
        Log.d(TAG, channel);
        List<News> bunchOfNews = null;
        try {
            bunchOfNews = mNewsApi.getNews(NewspaperHelper.getTypeChannel(channel),
                    String.valueOf(timeInMillis),
                    NewspaperRepository.MAX_NEWS)
                    .execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bunchOfNews == null) {
                bunchOfNews = new ArrayList<>(0);
            }
        }
        return bunchOfNews;
    }


    @Override
    public boolean saveBunchOfNews(List<News> bunchOfNews) {
        return false;
    }

    @Override
    public void refreshNews(String channel) {

    }
}
