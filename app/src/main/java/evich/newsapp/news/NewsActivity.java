package evich.newsapp.news;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import evich.newsapp.BaseActivity;
import evich.newsapp.NewsApplication;
import evich.newsapp.R;
import evich.newsapp.dagger.component.DaggerNewsActivityComponent;
import evich.newsapp.dagger.component.NewsActivityComponent;
import evich.newsapp.dagger.module.NewsActivityModule;
import evich.newsapp.helper.NewspaperHelper;

public class NewsActivity extends BaseActivity {

    @BindView(R.id.news_pager)
    ViewPager mNewsPager;

    @Inject
    NewsPresenter mNewsPresenter;

    private NewsPagerAdapter mPagerAdapter;

    public NewsActivityComponent getActivityComponent() {
        return DaggerNewsActivityComponent.builder()
                .applicationComponent(((NewsApplication) getApplication())
                .getApplicationComponent())
                .newsActivityModule(new NewsActivityModule(this))
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_news);

        ButterKnife.bind(this);
        getWindow().setBackgroundDrawable(null);
        getActivityComponent().inject(this);

        initialize();
    }

    private void initialize() {
        initializeToolbar();
        initializeViews();
    }

    private void initializeViews() {
        mNewsPager.setOffscreenPageLimit(NewspaperHelper.NUM_OF_CHANNELS);
        mPagerAdapter = new NewsPagerAdapter(getSupportFragmentManager());
        mNewsPager.setAdapter(mPagerAdapter);

        PagerSlidingTabStrip pagerTabStrip = (PagerSlidingTabStrip) findViewById(R.id.pager_tab_strip);
        pagerTabStrip.setViewPager(mNewsPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNewsPresenter.start();
    }

    @Override
    protected void onDestroy() {
        mNewsPresenter.finish();
        super.onDestroy();
    }

    private class NewsPagerAdapter extends FragmentStatePagerAdapter {

        private String[] pageTitles;

        public NewsPagerAdapter(FragmentManager fm) {
            super(fm);
            pageTitles = NewspaperHelper.getNewsChannels();
        }

        @Override
        public Fragment getItem(int position) {
            String channel = pageTitles[position];

            NewsFragment fragment = (NewsFragment) NewsFragment.getInstance(channel);
            mNewsPresenter.attachViewByChannel(channel, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            String channel = pageTitles[position];
            mNewsPresenter.detachViewByChannel(channel);

            super.destroyItem(container, position, object);
            Log.d("Newspaper", "destroyItem " + position);
        }
    }
}
