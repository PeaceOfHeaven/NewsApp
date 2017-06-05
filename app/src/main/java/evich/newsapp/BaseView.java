package evich.newsapp;

/**
 * Created by W8-64 on 14/05/2016.
 */
public interface BaseView<T> {

    void setPresenter(T presenter);

    T getPresenter();
}
