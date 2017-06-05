package evich.newsapp.data.source.remote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import evich.newsapp.data.News;
import evich.newsapp.data.source.NewspaperDataSource;
import evich.newsapp.data.source.local.NewsJsonParser;
import evich.newsapp.helper.NewspaperHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by W8-64 on 14/05/2016.
 */
public class NewspaperRemoteDataSource implements NewspaperDataSource {

    private static NewspaperRemoteDataSource mNewspaperRemoteDataSource;
    private Context mContext;

    private NewspaperRemoteDataSource(Context context) {
        mContext = context;
    }

    public static NewspaperRemoteDataSource getInstance(Context context) {
        if (mNewspaperRemoteDataSource == null) {
            mNewspaperRemoteDataSource = new NewspaperRemoteDataSource(context);
        }
        return mNewspaperRemoteDataSource;
    }

    @Nullable
    @Override
    public List<News> getNewsByChannel(String channel) {
        checkNotNull(channel);
        return fetchNewsByChannelWithTime(channel, System.currentTimeMillis());
    }

    public List<News> loadMoreNewsByChannel(String channel, long timeInMillis) {
        return fetchNewsByChannelWithTime(channel, timeInMillis);
    }

    private List<News> fetchNewsByChannelWithTime(String channel, long timeInMillis) {
        checkNotNull(channel);

        List<News> bunchOfNews = null;

        // http://quangtin.esy.es/docbao/layBaoByTypeOfNewAndChannel.php?typenew=0&typechannel=0&page=1&num_row_of_page=5

        String url = buildUrlWithParams(channel, timeInMillis);
        HttpURLConnection connection = null;
        InputStream in = null;

        try {
            connection = buildHttpConnectionFromUrl(url);
            in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();
            String stringResponse = "";
            while((stringResponse = reader.readLine()) != null) {
                builder.append(stringResponse);
            }
            Log.d("News", "Response : " + builder.toString());
            bunchOfNews = NewsJsonParser.parse(builder.toString());
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bunchOfNews;
    }

    private String buildUrlWithParams(final String channel, final long timeInMillis) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("typechannel", String.valueOf(NewspaperHelper.getTypeChannel(channel)));
        //parameters.put("page", "1");
        parameters.put("time", String.valueOf(timeInMillis));
        parameters.put("num_row_of_page", "10");

        String baseUrl = "http://quangtin.esy.es/docbao/layBaoByTypeOfChannelAndTime.php";
        StringBuilder builder = new StringBuilder();
        builder.append(baseUrl).append("?");

        Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
        while (true) {
            Map.Entry<String, String> param = iterator.next();
            builder.append(param.getKey()).append("=").append(param.getValue());
            if(!iterator.hasNext()) {
                break;
            }
            builder.append("&");
        }
        Log.d("News", builder.toString());
        return builder.toString();
    }

    private HttpURLConnection buildHttpConnectionFromUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return connection;
    }

    @Override
    public void saveSingleNews(@NonNull News news) {

    }

    @Override
    public void refreshNews(String channel, int newsRetrieveParams) {

    }

    @Override
    public void deleteAllNewsByChannel(String channel) {

    }

    @Override
    public void clearCachedNews() {

    }
}
