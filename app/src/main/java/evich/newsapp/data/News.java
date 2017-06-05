package evich.newsapp.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by W8-64 on 14/05/2016.
 */
public class News implements Parcelable {

    private String id;
    private int channel;
    private int newspaper;
    private String title;
    private String description;
    private String link;
    private String imgUrl;
    private String pubdate;

    public News() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(channel);
        dest.writeInt(newspaper);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(imgUrl);
        dest.writeString(pubdate);
    }

    public static final Creator<News> CREATOR
            = new Creator<News>() {
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        public News[] newArray(int size) {
            return new News[size];
        }
    };

    private News(Parcel in) {
        id = in.readString();
        channel = in.readInt();
        newspaper = in.readInt();
        title = in.readString();
        description = in.readString();
        link = in.readString();
        imgUrl = in.readString();
        pubdate = in.readString();
    }
}
