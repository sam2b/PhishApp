package tcss450.uw.edu.phishapp.blog;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to encapsulate a Phish.net Blog Post. Building an Object requires a publish date and title.
 *
 * Optional fields include URL, teaser, and Author.
 *
 *
 * @author Charles Bryan
 * @version 14 September 2018
 */
public class BlogPost implements Serializable {

    private final String mPubDate;
    private final String mTitle;
    private final String mUrl;
    private final String mTeaser;
    private final String mAuthor;

    /**
     * Helper class for building Credentials.
     *
     * @author Charles Bryan
     */
    public static class Builder {
        private final String mPubDate;
        private final String mTitle;
        private  String mUrl = "";
        private  String mTeaser = "";
        private  String mAuthor = "";


        /**
         * Constructs a new Builder.
         *
         * @param pubDate the published date of the blog post
         * @param title the title of the blog post
         */
        public Builder(String pubDate, String title) {
            this.mPubDate = pubDate;
            this.mTitle = title;
        }

        /**
         * Add an optional url for the full blog post.
         * @param val an optional url for the full blog post
         * @return the Builder of this BlogPost
         */
        public Builder addUrl(final String val) {
            mUrl = val;
            return this;
        }

        /**
         * Add an optional teaser for the full blog post.
         * @param val an optional url teaser for the full blog post.
         * @return the Builder of this BlogPost
         */
        public Builder addTeaser(final String val) {
            mTeaser = val;
            return this;
        }

        /**
         * Add an optional author of the blog post.
         * @param val an optional author of the blog post.
         * @return the Builder of this BlogPost
         */
        public Builder addAuthor(final String val) {
            mAuthor = val;
            return this;
        }

        public BlogPost build() {
            return new BlogPost(this);
        }

    }

    //Constructor
    private BlogPost(final Builder builder) {
        this.mPubDate = builder.mPubDate;
        this.mTitle = builder.mTitle;
        this.mUrl = builder.mUrl;
        this.mTeaser = cleanThatBitch(builder.mTeaser);
        this.mAuthor = builder.mAuthor;
    }

    public String getPubDate() {
        String dateString = mPubDate;
        try {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDateString = simpleDateFormat.format(new Date());
            Date niceDate = simpleDateFormat.parse(dateString);
            //TODO How to print the word day of the week, followed by mm-dd-YYYY
        } catch (Exception e) {

        } finally {
            return dateString;
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTeaser() {
        return mTeaser;
    }

    public String getAuthor() {
        return mAuthor;
    }

    // https://stackoverflow.com/questions/25660166/how-to-add-a-jar-in-external-libraries-in-android-studio
    private String cleanThatBitch(final String thatBitch) {
        String poop = thatBitch;
        //Create a white list of HTML tags that you want to keep. In this case, keep only
        // the <p> (paragraph) tags. This will remove <a> tags, <span> tags, etc.
        Whitelist whitelist = new Whitelist().addTags("p");

        //Clean the text using the white list
        poop = Jsoup.clean(poop, whitelist);
        //Because the white list kept the <p> tags, there is still html in the string.
        poop = poop.replace("<p>", "\n")
                .replace("</p>", "");

        return poop;
    }

}
