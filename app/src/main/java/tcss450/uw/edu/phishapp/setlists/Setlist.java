package tcss450.uw.edu.phishapp.setlists;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.Serializable;

/**
 * @author Sam Brendel
 */
public class Setlist implements Serializable {
    //TODO update comments to reflect a set list, not blog.
    private final String mLongDate;
    private final String mLocation;
    private final String mVenue;
    private final String mData;
    private final String mNotes;
    private final String mUrl;

    /**
     * Helper class for building the set list.
     * @author Sam Brendel
     */
    public static class Builder {
        private final String mLongDate;
        private final String mLocation;
        private String mVenue = "";
        private String mData = "";
        private String mNotes = "";
        private String mUrl = "";

        /**
         * Constructs a new Builder.
         * @param theLongDate the word day and date of the concert in the set list.
         * @param theLocation the location of the concert in the set list.
         */
        public Builder(final String theLongDate, final String theLocation) {
            this.mLongDate = theLongDate;
            this.mLocation = theLocation;
        }

        /**
         * Add an optional url for the full blog post.
         * @param val an optional url for the full blog post
         * @return the Builder of this BlogPost
         */
        public Builder addVenue(final String val) {
            mVenue = val;
            return this;
        }

        /**
         * Add an optional teaser for the full blog post.
         * @param val an optional url teaser for the full blog post.
         * @return the Builder of this BlogPost
         */
        public Builder addData(final String val) {
            mData = val;
            return this;
        }

        /**
         * Add an optional author of the blog post.
         * @param val an optional author of the blog post.
         * @return the Builder of this BlogPost
         */
        public Builder addNotes(final String val) {
            mNotes = val;
            return this;
        }

        public Builder addUrl(final String val) {
            mUrl = val;
            return this;
        }

        public Setlist build() {
            return new Setlist(this);
        }

    }

    //Constructor
    private Setlist(final Builder builder) {
        this.mLongDate = builder.mLongDate;
        this.mLocation = builder.mLocation;
        this.mVenue = builder.mVenue;
        this.mData = cleanThatBitch(builder.mData);
        this.mNotes = cleanThatBitch(builder.mNotes);
        this.mUrl = builder.mUrl;
    }

    public String getLongDate() {
        return this.mLongDate;
    }

    public String getLocation() {
        return this.mLocation;
    }

    public String getVenue() {
        return cleanThatBitch(this.mVenue);
    }

    public String getData() {
        return this.mData;
    }

    public String getNotes() {
        return this.mNotes;
    }

    public String getUrl() {
        return this.mUrl;
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
                .replace("</p>", "")
                .replace("&nbsp;", " ")
                .replace("&gt;", ">");

        return poop;
    }

}
