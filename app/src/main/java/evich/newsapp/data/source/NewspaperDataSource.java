package evich.newsapp.data.source;

import java.util.List;

import evich.newsapp.data.News;

/**
 * Created by W8-64 on 14/05/2016.
 */
public interface NewspaperDataSource {

    List<News> getNews(String channel);

    boolean saveBunchOfNews(List<News> bunchOfNews);

    void refreshNews(String channel);
}
