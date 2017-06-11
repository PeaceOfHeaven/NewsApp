package evich.newsapp.data;

import com.google.gson.annotations.SerializedName;

import evich.newsapp.helper.NewspaperHelper;

/**
 * Created by W8-64 on 14/05/2016.
 */
public class News {

    @SerializedName("id")
    private String id;

    @SerializedName("typechannel")
    private int channel;

    @SerializedName("typenew")
    private int newspaper;

    @SerializedName("title")
    private String title;

    private String description;

    @SerializedName("link")
    private String link;

    @SerializedName("image")
    private String imgUrl;

    @SerializedName("pubDate")
    private String pubdate;

    public News() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getChannelType() {
        return channel;
    }

    public void setChannelType(int channel) {
        this.channel = channel;
    }

    public String getChannelTitle() {
        return NewspaperHelper.getChannel(channel);
    }

    public int getNewspaperType() {
        return newspaper;
    }

    public void setNewspaperType(int newspaper) {
        this.newspaper = newspaper;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

}
