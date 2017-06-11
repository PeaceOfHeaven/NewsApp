package evich.newsapp.data.source.remote;

import java.util.List;

import evich.newsapp.data.News;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Nhat on 6/7/2017.
 */

public interface NewsApi {

    @GET(EndPoints.GET_NEWS_BY_TIME)
    Call<List<News>> getNews(@Query("typechannel") String channel
                        , @Query("time") String time
                        , @Query("num_row_of_page") int numPage);
}
