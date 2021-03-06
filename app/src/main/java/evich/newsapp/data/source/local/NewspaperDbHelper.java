package evich.newsapp.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import evich.newsapp.dagger.extras.ApplicationContext;

/**
 * Created by W8-64 on 08/06/2016.
 */
@Singleton
public class NewspaperDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "News.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String INTEGER_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";

    @Inject
    public NewspaperDbHelper(@ApplicationContext Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + NewsPersistenceContract.NewsEntry.TABLE_NAME + " (" +
                        NewsPersistenceContract.NewsEntry._ID + INTEGER_TYPE + " PRIMARY KEY," +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_NEWS_ID + TEXT_TYPE + COMMA_SEP +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_LINK + TEXT_TYPE + COMMA_SEP +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_IMG_URL + TEXT_TYPE + COMMA_SEP +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_PUBLIC_DATE + TEXT_TYPE + COMMA_SEP +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_CHANNEL + INTEGER_TYPE + COMMA_SEP +
                        NewsPersistenceContract.NewsEntry.COLUMN_NAME_NEWSPAPER + INTEGER_TYPE + COMMA_SEP +
                        " UNIQUE (" + NewsPersistenceContract.NewsEntry.COLUMN_NAME_NEWS_ID + ") ON CONFLICT REPLACE"
                        + ")";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NewsPersistenceContract.NewsEntry.TABLE_NAME);
        onCreate(db);
    }
}
