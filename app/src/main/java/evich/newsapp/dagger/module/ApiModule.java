package evich.newsapp.dagger.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import evich.newsapp.data.source.remote.EndPoints;
import evich.newsapp.data.source.remote.NewsApi;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Nhat on 6/9/2017.
 */
@Module
public class ApiModule {

    @Provides
    public NewsApi provideNewsApi() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EndPoints.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(NewsApi.class);
    }
}
