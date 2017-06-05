package evich.newsapp.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import evich.newsapp.data.News;
import evich.newsapp.data.source.NewspaperDataSource;
import evich.newsapp.helper.NewspaperHelper;

import static com.google.common.base.Preconditions.checkNotNull;
import static evich.newsapp.data.source.local.NewsPersistenceContract.*;

/**
 * Created by W8-64 on 08/06/2016.
 */
public class NewspaperLocalDataSource implements NewspaperDataSource {

    private static NewspaperLocalDataSource mNewspaperLocalDataSource;

    private NewspaperDbHelper mDbHelper;

    private SQLiteDatabase mDb;

    public static NewspaperLocalDataSource getInstance(@NonNull Context context) {
        if (mNewspaperLocalDataSource == null) {
            mNewspaperLocalDataSource = new NewspaperLocalDataSource(context);
        }
        return mNewspaperLocalDataSource;
    }

    private NewspaperLocalDataSource(Context context) {
        checkNotNull(context);

        mDbHelper = new NewspaperDbHelper(context);
        mDb = mDbHelper.getWritableDatabase();
    }

    @Nullable
    @Override
    public List<News> getNewsByChannel(String channel) {
        List<News> bunchOfNews = new ArrayList<News>();
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

            Cursor c = mDb.query(
                    NewsEntry.TABLE_NAME, projection, NewsEntry.COLUMN_NAME_CHANNEL + "=?", new
                            String[]{
                            String.valueOf(NewspaperHelper.getTypeChannel(channel))
                    }, null, null, NewsEntry.COLUMN_NAME_PUBLIC_DATE + " DESC");

            if (c != null && c.getCount() > 0) {
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
                    news.setChannel(channel_type);
                    news.setNewspaperType(newspaper_type);
                    bunchOfNews.add(news);
                }
            }
            if (c != null) {
                c.close();
            }

        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
        return bunchOfNews;
    }

    @Override
    public void saveSingleNews(@NonNull News news) {
        try {
            checkNotNull(news);

            ContentValues values = new ContentValues();
            values.put(NewsEntry.COLUMN_NAME_NEWS_ID, news.getId());
            values.put(NewsEntry.COLUMN_NAME_TITLE, news.getTitle());
            values.put(NewsEntry.COLUMN_NAME_DESCRIPTION, news.getDescription());
            values.put(NewsEntry.COLUMN_NAME_LINK, news.getLink());
            values.put(NewsEntry.COLUMN_NAME_IMG_URL, news.getImgUrl());
            values.put(NewsEntry.COLUMN_NAME_PUBLIC_DATE, news.getPubdate());
            values.put(NewsEntry.COLUMN_NAME_CHANNEL, news.getChannel());
            values.put(NewsEntry.COLUMN_NAME_NEWSPAPER, news.getNewspaperType());

            mDb.insert(NewsEntry.TABLE_NAME, null, values);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void refreshNews(String channel, int newsRetrieveParams) {

    }

    @Override
    public void deleteAllNewsByChannel(String channel) {

    }

    @Override
    public void clearCachedNews() {
        /* delete from tb_news where newsid IN
        (SELECT newsid from tb_news order by newsid desc limit 10)*/
        String[] channels = NewspaperHelper.getNewsChannels();
        for(int i=0; i < channels.length; i++) {
            mDb.execSQL("DELETE FROM " + NewsEntry.TABLE_NAME + " WHERE " + NewsEntry
                    .COLUMN_NAME_NEWS_ID
                    + " NOT IN (SELECT " + NewsEntry.COLUMN_NAME_NEWS_ID + " FROM " + NewsEntry
                    .TABLE_NAME + " WHERE " + NewsEntry.COLUMN_NAME_CHANNEL + "=" + NewspaperHelper
                    .getTypeChannel(channels[i]) + " ORDER BY " + NewsEntry
                    .COLUMN_NAME_PUBLIC_DATE
                    + " DESC LIMIT 20 OFFSET 0)");

        }
    }
}
