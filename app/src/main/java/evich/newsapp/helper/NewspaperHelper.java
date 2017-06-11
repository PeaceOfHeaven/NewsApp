package evich.newsapp.helper;

/**
 * Created by W8-64 on 14/05/2016.
 */
public class NewspaperHelper {

    public static final int NUM_OF_CHANNELS = 4;
    public static final String CHANNEL_EDUCATION = "Giáo Dục";
    public static final String CHANNEL_TECH = "Công Nghệ";
    public static final String CHANNEL_LAW = "Pháp Luật";
    public static final String CHANNEL_ENTERTAINMENT = "Giải Trí";

    public static int getTypeChannel(String channel) {
        switch (channel) {
            case CHANNEL_EDUCATION :
                return 0;
            case CHANNEL_TECH :
                return 1;
            case CHANNEL_LAW :
                return 2;
            case CHANNEL_ENTERTAINMENT :
                return 3;
        }
        return -1;
    }

    public static String getChannel(int type) {
        switch (type) {
            case 0 :
                return CHANNEL_EDUCATION;
            case 1 :
                return CHANNEL_TECH;
            case 2 :
                return CHANNEL_LAW;
            case 3 :
                return CHANNEL_ENTERTAINMENT;
        }
        return null;
    }

    public static String[] getNewsChannels() {
        return new String[]{CHANNEL_EDUCATION, CHANNEL_TECH, CHANNEL_LAW, CHANNEL_ENTERTAINMENT};
    }
}
