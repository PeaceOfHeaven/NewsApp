package evich.newsapp.data.source.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import evich.newsapp.data.News;
import evich.newsapp.data.source.NewspaperDataSource;
import evich.newsapp.helper.NewspaperHelper;

import static com.google.common.base.Preconditions.checkNotNull;
import static evich.newsapp.data.source.local.NewsPersistenceContract.NewsEntry;

/**
 * Created by W8-64 on 08/06/2016.
 */
@Singleton
public class NewspaperLocalDataSource implements NewspaperDataSource {

    private static final String TAG = NewspaperLocalDataSource.class.getSimpleName();
    private NewspaperDbHelper mDbHelper;

    @Inject
    public NewspaperLocalDataSource(NewspaperDbHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    @Nullable
    @Override
    public List<News> getNews(String channel) {
        List<News> bunchOfNews = new ArrayList<News>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        try {
            String[] projection = {
                    NewsEntry.COLUMN_NAME_NEWS_ID,
                    NewsEntry.COLUMN_NAME_TITLE,
                    NewsEntry.COLUMN_NAME_LINK,
                    NewsEntry.COLUMN_NAME_IMG_URL,
                    NewsEntry.COLUMN_NAME_DESCRIPTION,
                    NewsEntry.COLUMN_NAME_PUBLIC_DATE,
                    NewsEntry.COLUMN_NAME_CHANNEL,
                    NewsEntry.COLUMN_NAME_NEWSPAPER
            };

            Cursor c = db.query(
                    NewsEntry.TABLE_NAME, projection, NewsEntry.COLUMN_NAME_CHANNEL + "=?", new
                            String[]{
                            String.valueOf(NewspaperHelper.getTypeChannel(channel))
                    }, null, null, NewsEntry.COLUMN_NAME_PUBLIC_DATE + " DESC");

            if (c != null && c.getCount() > 0) {
                Log.d(TAG, "Retrieve: " + channel + "-" + c.getCount());

                while (c.moveToNext()) {
                    String newsId = c
                            .getString(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_NEWS_ID));
                    String title = c
                            .getString(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_TITLE));
                    String description =
                            c.getString(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_DESCRIPTION));
                    String link =
                            c.getString(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_LINK));
                    String imgUrl =
                            c.getString(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_IMG_URL));
                    String publicDate =
                            c.getString(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_PUBLIC_DATE));
                    int channel_type =
                            c.getInt(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_CHANNEL));
                    int newspaper_type =
                            c.getInt(c.getColumnIndexOrThrow(NewsEntry.COLUMN_NAME_NEWSPAPER));
                    News news = new News();
                    news.setId(newsId);
                    news.setTitle(title);
                    news.setDescription(description);
                    news.setLink(link);
                    news.setImgUrl(imgUrl);
                    news.setPubdate(publicDate);
                    news.setChannelType(channel_type);
                    news.setNewspaperType(newspaper_type);
                    bunchOfNews.add(news);
                }
            }
            if (c != null) {
                c.close();
            }
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
            Log.e(TAG, "Get news failed!", e);
        }
        return bunchOfNews;
    }

    @Override
    public boolean saveBunchOfNews(List<News> bunchOfNews) {
        checkNotNull(bunchOfNews);

        if(!bunchOfNews.isEmpty()) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                clearOldNews(db, bunchOfNews.get(0).getChannelType());
                for (News news : bunchOfNews) {
                    ContentValues values = new ContentValues();
                    values.put(NewsEntry.COLUMN_NAME_NEWS_ID, news.getId());
                    values.put(NewsEntry.COLUMN_NAME_TITLE, news.getTitle());
                    values.put(NewsEntry.COLUMN_NAME_DESCRIPTION, news.getDescription());
                    values.put(NewsEntry.COLUMN_NAME_LINK, news.getLink());
                    values.put(NewsEntry.COLUMN_NAME_IMG_URL, news.getImgUrl());
                    values.put(NewsEntry.COLUMN_NAME_PUBLIC_DATE, news.getPubdate());
                    values.put(NewsEntry.COLUMN_NAME_CHANNEL, news.getChannelType());
                    values.put(NewsEntry.COLUMN_NAME_NEWSPAPER, news.getNewspaperType());

                    db.insert(NewsEntry.TABLE_NAME, null, values);
                }
                db.setTransactionSuccessful();
                return true;
            } catch (IllegalStateException e) {
                Log.e(TAG, "Insert failed", e);
            } finally {
                db.endTransaction();
            }
        }
        return false;
    }

    private void clearOldNews(SQLiteDatabase db, int channelType) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        db.delete(NewsEntry.TABLE_NAME, NewsEntry.COLUMN_NAME_PUBLIC_DATE + "<=? AND "
                                        + NewsEntry.COLUMN_NAME_CHANNEL + "=?",
                new String[]{Long.toString(currentTime), Integer.toString(channelType)});
    }

    @Override
    public void refreshNews(String channel) {
        throw new UnsupportedOperationException("Invalid operation for refresh news");
    }

}
