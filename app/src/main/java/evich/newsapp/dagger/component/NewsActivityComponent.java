package evich.newsapp.dagger.component;

import dagger.Component;
import evich.newsapp.dagger.extras.PerActivity;
import evich.newsapp.dagger.module.NewsActivityModule;
import evich.newsapp.news.NewsActivity;

/**
 * Created by Nhat on 6/9/2017.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = NewsActivityModule.class)
public interface NewsActivityComponent extends ActivityInjector<NewsActivity> {

}
