package evich.newsapp.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import evich.newsapp.data.News;

/**
 * Created by W8-64 on 14/05/2016.
 */
public interface NewspaperDataSource {

    @Nullable
    public List<News> getNewsByChannel(String channel);

    public void saveSingleNews(@NonNull News news);

    public void refreshNews(String channel, int newsRetrieveParams);

    public void deleteAllNewsByChannel(String channel);

    public void clearCachedNews();
}
