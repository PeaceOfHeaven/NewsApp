package evich.newsapp.news;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import evich.newsapp.BaseActivity;
import evich.newsapp.R;
import evich.newsapp.data.source.NewspaperRepository;
import evich.newsapp.data.source.local.NewspaperLocalDataSource;
import evich.newsapp.data.source.remote.NewspaperRemoteDataSource;
import evich.newsapp.helper.NewspaperHelper;

public class NewsActivity extends BaseActivity {

    private NewsContract.Presenter mNewsPresenter;
    private NewspaperRepository mRepository;

    // private TabLayout mTabLayout;
    private ViewPager mPager;
    private NewsPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_news);

        // Create the presenter
        NewspaperRemoteDataSource newspaperRemoteDataSource = NewspaperRemoteDataSource
                .getInstance(this);
        NewspaperLocalDataSource newspaperLocalDataSource = NewspaperLocalDataSource.getInstance
                (this);

        mRepository = NewspaperRepository.getInstance(newspaperRemoteDataSource,
                newspaperLocalDataSource);

        mNewsPresenter = new NewsPresenter(this, getSupportLoaderManager
                (), mRepository);

        initialize();
    }

    private void initialize() {
        initializeToolbar();
        initializeViews();
    }

    private void initializeViews() {
        // mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(3);
        mPagerAdapter = new NewsPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mPager);
        // mTabLayout.setupWithViewPager(mPager);
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
