package evich.newsapp.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.view.Window;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;

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

    private static final String TAG = NewsActivity.class.getSimpleName();

    @BindView(R.id.news_pager)
    ViewPager mNewsPager;

    @Inject
    NewsPresenter mNewsPresenter;

    private NewsPagerAdapter mPagerAdapter;

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        int currentPosition;
        boolean dontLoadList;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentPosition = position;
            if (positionOffset == 0 && positionOffsetPixels == 0) { // the offset is zero when the swiping ends{
                dontLoadList = false;
            } else {
                dontLoadList = true; // To avoid loading content for list after swiping the pager.
            }
        }

        @Override
        public void onPageSelected(int position) {
            updatePage(position, false);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) { // the viewpager is idle as swipping ended
                if(!dontLoadList) {
                    updatePage(currentPosition, true);
                }
            }
        }
    };

    public NewsActivityComponent getActivityComponent() {
        return DaggerNewsActivityComponent.builder()
                .applicationComponent(((NewsApplication) getApplication())
                        .getApplicationComponent())
                .newsActivityModule(new NewsActivityModule(this))
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_news);

        ButterKnife.bind(this);
        getWindow().setBackgroundDrawable(null);
        getActivityComponent().inject(this);

        mPagerAdapter = new NewsPagerAdapter(getSupportFragmentManager());
        mNewsPager.setAdapter(mPagerAdapter);
        mNewsPager.setOffscreenPageLimit(2); // a huge amount of pages, we need this to improve perf while swipping

        PagerSlidingTabStrip pagerTabStrip = (PagerSlidingTabStrip) findViewById(R.id.pager_tab_strip);
        pagerTabStrip.setViewPager(mNewsPager);
        pagerTabStrip.setOnPageChangeListener(mOnPageChangeListener);

        initializeToolbar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNewsPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNewsPresenter.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // This is important : Hack to open a dummy activity for 200-500ms (cannot be noticed by user as it is for 500ms
        //  and transparent floating activity and auto finishes)
        startActivity(new Intent(this, DummyActivity.class));
        finish();
    }

    private void updatePage(int position, boolean enable) {
        if (position < 0 && position >= mPagerAdapter.getCount()) {
            return;
        }

        for (int offset = 0; offset <= mNewsPager.getOffscreenPageLimit(); offset++) {
            if (position + offset < mPagerAdapter.getCount()) {
                NewsFragment backwardPage = mPagerAdapter.getNewsFragmentAt(position + offset);
                if (backwardPage != null) {
                    backwardPage.setShouldUpdateNow(enable);
                }
            }
            if (position - offset > 0) {
                NewsFragment forwardPage = mPagerAdapter.getNewsFragmentAt(position - offset);
                if (forwardPage != null) {
                    forwardPage.setShouldUpdateNow(enable);
                }
            }
        }
    }

    private class NewsPagerAdapter extends FragmentStatePagerAdapter {

        private String[] pageTitles;
        private ArrayList<NewsFragment> mNewsFragments;

        public NewsPagerAdapter(FragmentManager fm) {
            super(fm);
            pageTitles = NewspaperHelper.getNewsChannels();
            mNewsFragments = new ArrayList<>(getCount());
            for (int i = 0; i < getCount(); i++) {
                mNewsFragments.add(null);
            }
        }

        @Override
        public Fragment getItem(int position) {
            String channel = pageTitles[position];
            Fragment fragment = null;
            if (mNewsFragments.get(position) == null) {
                fragment = NewsFragment.getInstance(channel);
            }
            return fragment;
        }

        /**
         * Returns the fragment at the specified position in this adapter.
         *
         * @param position position of the fragment to return
         * @return the fragment at the specified position in this adapter
         * @throws IndexOutOfBoundsException {@inheritDoc}
         */
        public NewsFragment getNewsFragmentAt(int position) {
            return mNewsFragments.get(position);
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String channel = pageTitles[position];
            NewsFragment fragment = (NewsFragment) super.instantiateItem(container, position);
            mNewsPresenter.attachViewByChannel(channel, fragment);
            mNewsFragments.set(position, fragment);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            String channel = pageTitles[position];
            mNewsPresenter.detachViewByChannel(channel);
            mNewsFragments.set(position, null);
            super.destroyItem(container, position, object);
        }
    }
}
