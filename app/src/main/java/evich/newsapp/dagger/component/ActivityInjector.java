package evich.newsapp.dagger.component;

/**
 * Created by Nhat on 6/9/2017.
 */

public interface ActivityInjector<A> {

    void inject(A activity);
}
