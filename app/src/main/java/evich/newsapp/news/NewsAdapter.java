package evich.newsapp.news;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import evich.newsapp.R;
import evich.newsapp.data.News;

/**
 * Created by Nhat on 6/29/2017.
 */

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = NewsAdapter.class.getSimpleName();
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private static final Integer UPDATE_THUMB = new Integer(8);

    private List<News> mBunchOfNews;
    private final NewsItemListener mNewspaperItemListener;

    public interface NewsItemListener {
        void onNewsClicked(News news);
    }

    public NewsAdapter(final List<News> bunchOfNews, final NewsItemListener
            newspaperItemListener) {
        mBunchOfNews = bunchOfNews;
        mNewspaperItemListener = newspaperItemListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder holder;

        if (viewType == VIEW_TYPE_ITEM) {
            View view = inflater.inflate(R.layout.news_item, parent, false);
            holder = new ViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.news_loading_item, parent, false);
            holder = new LoadingViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final News news = mBunchOfNews.get(position);

        if (holder instanceof ViewHolder) {
            ViewHolder newsViewHolder = (ViewHolder) holder;
            Context context = newsViewHolder.itemView.getContext();

            if (!TextUtils.isEmpty(news.getImgUrl())) {
                Picasso.with(context).load(news.getImgUrl())
                        .into(newsViewHolder.newsImageImgView);
                newsViewHolder.newsImageImgView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                Picasso.with(context).load(android.R.color.transparent)
                        .into(newsViewHolder.newsImageImgView);
                newsViewHolder.newsImageImgView.setBackgroundColor(Color.TRANSPARENT);
            }
            newsViewHolder.newsTitleTxtView.setText(news.getTitle());

            String convertedDate = (String) DateUtils
                    .getRelativeDateTimeString(holder.itemView.getContext(), Long.parseLong
                                    (news.getPubdate()) * 1000,
                            DateUtils.SECOND_IN_MILLIS,
                            DateUtils.WEEK_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_TIME);
            newsViewHolder.newsPubdateTxtView.setText(convertedDate);

            newsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNewspaperItemListener.onNewsClicked(news);
                }
            });
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            final News news = mBunchOfNews.get(position);
            final ViewHolder newsViewHolder = (ViewHolder) holder;
            Context context = newsViewHolder.itemView.getContext();

            if (!TextUtils.isEmpty(news.getImgUrl())) {
                Picasso.with(context).load(news.getImgUrl())
                        .into(newsViewHolder.newsImageImgView, new Callback() {
                            @Override
                            public void onSuccess() {
                                ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(newsViewHolder.newsImageImgView
                                        , View.ALPHA, 0, 1);
                                alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                                alphaAnimation.start();
                            }

                            @Override
                            public void onError() {

                            }
                        });
                newsViewHolder.newsImageImgView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                Picasso.with(context).load(android.R.color.transparent)
                        .into(newsViewHolder.newsImageImgView);
                newsViewHolder.newsImageImgView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mBunchOfNews.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mBunchOfNews.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void replaceData(List<News> bunchOfNews) {
        final NewsDiffCallback diffCallback
                = new NewsDiffCallback(mBunchOfNews, bunchOfNews);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        mBunchOfNews.clear();
        mBunchOfNews.addAll(bunchOfNews);
        diffResult.dispatchUpdatesTo(this);
    }

    public List<News> getData() {
        return mBunchOfNews;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.newsImg_imgView)
        public ImageView newsImageImgView;

        @BindView(R.id.newsTitle_txtView)
        public TextView newsTitleTxtView;

        @BindView(R.id.newsPublicDate_txtView)
        public TextView newsPubdateTxtView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.loadingMoreProgressBar)
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class NewsDiffCallback extends DiffUtil.Callback {

        private final List<News> mOldNewsList;
        private final List<News> mNewNewsList;

        public NewsDiffCallback(List<News> oldNewsList, List<News> newNewsList) {
            mOldNewsList = oldNewsList;
            mNewNewsList = newNewsList;
        }

        @Override
        public int getOldListSize() {
            return mOldNewsList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewNewsList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldNewsList.get(oldItemPosition).getId()
                    .equals(mNewNewsList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            /*return mOldNewsList.get(oldItemPosition).getId()
                    .equals(mNewNewsList.get(newItemPosition));*/
            return false;
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return UPDATE_THUMB;
        }
    }
}
