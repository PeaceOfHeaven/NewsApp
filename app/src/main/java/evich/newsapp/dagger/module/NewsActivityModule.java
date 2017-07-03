package evich.newsapp.dagger.module;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;
import evich.newsapp.dagger.extras.ActivityContext;

/**
 * Created by Nhat on 6/9/2017.
 */
@Module
public class NewsActivityModule {

    private AppCompatActivity mActivity;

    public NewsActivityModule(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return mActivity;
    }

    @Provides
    LoaderManager provideSupportLoaderManager() {
        return mActivity.getSupportLoaderManager();
    }

}
