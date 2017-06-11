package evich.newsapp.dagger.component;

import javax.inject.Singleton;

import dagger.Component;
import evich.newsapp.dagger.module.ApiModule;
import evich.newsapp.dagger.module.ApplicationModule;
import evich.newsapp.data.source.NewspaperRepository;
import evich.newsapp.data.source.remote.NewsApi;

/**
 * Created by Nhat on 6/8/2017.
 */
@Singleton
@Component(modules = {ApplicationModule.class, ApiModule.class})
public interface ApplicationComponent {

    NewspaperRepository getNewspaperRepository();

    NewsApi getNewsApi();
}
