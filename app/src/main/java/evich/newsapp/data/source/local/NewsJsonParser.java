package evich.newsapp.data.source.local;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import evich.newsapp.data.News;

/**
 * Created by W8-64 on 01/06/2016.
 */
public class NewsJsonParser {

    public static List<News> parse(String json) {
        List<News> bunchOfNews = new ArrayList<>();

        try {
            JSONArray bunchOfNewsJsonArray = new JSONArray(json);
            int length = bunchOfNewsJsonArray.length();

            if(length > 0) {
                for (int i = 0; i < length; i++) {
                    JSONObject newsJsonObject = bunchOfNewsJsonArray.getJSONObject(i);

                    News news = new News();
                    news.setId(newsJsonObject.getString("id"));
                    news.setTitle(newsJsonObject.getString("title"));
                    news.setDescription("");
                    news.setLink(newsJsonObject.getString("link"));
                    news.setImgUrl(newsJsonObject.getString("image"));
                    news.setPubdate(newsJsonObject.getString("pubDate"));
                    news.setChannelType(newsJsonObject.getInt("typechannel"));
                    news.setNewspaperType(newsJsonObject.getInt("typenew"));

                    bunchOfNews.add(news);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bunchOfNews;
    }
}
