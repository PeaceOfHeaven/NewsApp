package evich.newsapp.dagger.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import evich.newsapp.NewsApplication;
import evich.newsapp.dagger.extras.ApplicationContext;
import evich.newsapp.dagger.extras.Local;
import evich.newsapp.dagger.extras.Remote;
import evich.newsapp.data.source.NewspaperDataSource;
import evich.newsapp.data.source.local.NewspaperDbHelper;
import evich.newsapp.data.source.local.NewspaperLocalDataSource;
import evich.newsapp.data.source.remote.NewspaperRemoteDataSource;

/**
 * Created by Nhat on 6/8/2017.
 */
@Module
public class ApplicationModule {

    private final NewsApplication mNewsApplication;

    public ApplicationModule(NewsApplication newsApplication) {
        mNewsApplication = newsApplication;
    }

    @Provides
    @ApplicationContext
    public Context provideContext() {
        return mNewsApplication;
    }

    @Provides
    public NewsApplication provideApplication() {
        return mNewsApplication;
    }

    @Singleton
    @Provides
    @Local
    NewspaperDataSource provideNewsLocalDataSource(NewspaperDbHelper dbHelper) {
        return new NewspaperLocalDataSource(dbHelper);
    }

    @Singleton
    @Provides
    @Remote
    NewspaperDataSource provideNewsRemoteDataSource() {
        return new NewspaperRemoteDataSource();
    }

    @Singleton
    @Provides
    NewspaperDbHelper provideNewspaperDbHelper(@ApplicationContext Context context) {
        return new NewspaperDbHelper(context);
    }
}
