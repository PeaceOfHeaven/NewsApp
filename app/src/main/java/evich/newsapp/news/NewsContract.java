package evich.newsapp.news;


import java.util.List;

import evich.newsapp.BasePresenter;
import evich.newsapp.BaseView;
import evich.newsapp.data.News;

/**
 * Created by W8-64 on 15/05/2016.
 */
public interface NewsContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void setLoadingMoreIndicator(boolean active);

        void setRefreshIndicator(boolean active);

        void showNews(List<News> bunchOfNews);

        void showLoadingNewsError();

        void showNewsDetailUi(String link);
    }

    interface Presenter extends BasePresenter {

        void attachViewByChannel(String channel, View view);

        void detachViewByChannel(String channel);

        void loadNewsByChannel(String channel, int newsRetrieveParams);

        void openNewsDetail(News requestedNews);
    }
}
