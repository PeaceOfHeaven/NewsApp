package evich.newsapp.data.source.local;

import android.provider.BaseColumns;

/**
 * Created by W8-64 on 08/06/2016.
 */
public class NewsPersistenceContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public NewsPersistenceContract() {}

    /* Inner class that defines the table contents */
    public static abstract class NewsEntry implements BaseColumns {

        public static final String TABLE_NAME = "news";

        public static final String COLUMN_NAME_NEWS_ID = "news_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_NAME_IMG_URL = "img_url";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_PUBLIC_DATE = "pub_date";
        public static final String COLUMN_NAME_CHANNEL = "channel_type";
        public static final String COLUMN_NAME_NEWSPAPER = "newspaper_type";
    }
}
