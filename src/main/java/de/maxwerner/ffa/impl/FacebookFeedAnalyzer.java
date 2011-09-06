package de.maxwerner.ffa.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.maxwerner.ffa.FeedAnalyzer;

/**
 * @author Max Jonas Werner <mail@makk.es>
 */
public class FacebookFeedAnalyzer implements FeedAnalyzer {
    
    private final static DateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
//    private final static String APP_ID = "147225311985489";
//    private final static String APP_SECRET = "1cf6e21e1f05d6439128c50969e9c6bd";
    
    private final static String FEED_URL_PATTERN = "https://graph.facebook.com/__PROFILE__/feed";
    private final static String PAGE_URL_PATTERN = "https://graph.facebook.com/__PROFILE__";
    
    private final String feedUrl;
    private final String pageUrl;
    
    public FacebookFeedAnalyzer(final String profile) {
        feedUrl = FEED_URL_PATTERN.replaceAll("__PROFILE__", profile);
        pageUrl = PAGE_URL_PATTERN.replace("__PROFILE__", profile);
    }
    
    public Long getPageId() throws MalformedURLException, IOException, JSONException {
        final JSONObject raw = getJSON(pageUrl);
        return new Long(raw.getLong("id"));
    }

    /**
     * Extracts a set of messages from a feed which have been created
     * after 'from' and before 'to'. Important: the parameters 'from' and
     * 'to' are exclusive.
     * 
     * @param feed the feed to extract messages from
     * @param from message must have been created after this date
     * @param to message must have been created before this date
     * @return the set of messages
     * @throws ParseException
     * @throws JSONException
     * @throws IOException 
     * @throws MalformedURLException 
     */
    @Override
    public List<JSONObject> getMessagesBetween(final JSONObject feed, final Date from, final Date to) throws ParseException,
            JSONException, MalformedURLException, IOException {

      final List<JSONObject> result = new ArrayList<JSONObject>();
      final JSONArray feedData = feed.getJSONArray("data");
      if(feedData.length() == 0) {
          return result;
      }
        
        for (int i = 0; i < feedData.length(); i++) {
            final Date createdTime = INPUT_DATE_FORMAT.parse(feedData.getJSONObject(i).getString("created_time"));
            if(createdTime.before(to) && createdTime.after(from)) {
                result.add(feedData.getJSONObject(i));
            }
        }
        if(from.before(INPUT_DATE_FORMAT.parse(feedData.getJSONObject(feedData.length()-1).getString("created_time")))) {
            final List<JSONObject> nextResult = getMessagesBetween(getNextPage(feed), from, to);
            result.addAll(nextResult);
        }
        return result;
    }
    
    @Override
    public JSONObject getNextPage(final JSONObject feed) throws JSONException, MalformedURLException, IOException {
        final String nextUrl = feed.getJSONObject("paging").getString("next");
        return getJSON(nextUrl);
    }
    
    private JSONObject getJSON(final String url) throws MalformedURLException, IOException, JSONException {
        final InputStream is = new URL(url).openStream();
        final int blockSize = 8192;
        final byte[] chunk = new byte[blockSize];
        int chunkSize;
        final StringBuffer rawJson = new StringBuffer();
        while ((chunkSize = is.read(chunk)) != -1) {
            rawJson.append(new String(chunk, 0, chunkSize));
        }
        return new JSONObject(rawJson.toString());
    }
    
    @Override
    public JSONObject getFeed() throws IOException, JSONException {
        return getJSON(feedUrl);
    }

    @Override
    public Long getCommentCount(final JSONObject message) throws JSONException {
        final JSONObject comments = message.optJSONObject("comments");
        if(comments == null) {
            return 0L;
        }
        return comments.getLong("count");
    }

    @Override
    public Long getLikeCount(JSONObject message) throws JSONException {
        final Long likes = message.optLong("likes");
        return likes == null ? 0L : likes;
    }

}
