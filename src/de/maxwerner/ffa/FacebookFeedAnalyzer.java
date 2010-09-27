/**
 * Created at 31 Aug 2010 11:27:27
 * Created by max
 *
 * (c) Copyright 2010 just software AG
 *
 * This file contains unpublished, proprietary trade secret information of
 * just software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * just software AG.
 */

package de.maxwerner.ffa;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 *
 * @author Max Jonas Werner <max.jonas.werner@justsoftwareag.com>
 */
public class FacebookFeedAnalyzer {
    
    private final static DateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private final static DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void main(String[] args) throws IOException, JSONException, ParseException {
        final FacebookFeedAnalyzer analyzer = new FacebookFeedAnalyzer();
        final URL baseUrl = new URL("https://graph.facebook.com/weltkompakt/feed");
        final JSONObject feed = analyzer.getFeed(baseUrl);

        final Calendar from = Calendar.getInstance();
        final Calendar to = Calendar.getInstance();
        from.set(2010, Calendar.AUGUST, 27, 23, 59, 59);
        to.set(2010, Calendar.SEPTEMBER, 26, 0, 0, 0);
        final List<JSONObject> messages = analyzer.getMessagesBetween(feed, from.getTime(), to.getTime());
        System.out.println("Total messages: " + messages.size());
        
        final Map<String, Integer> messageCountByType = new HashMap<String, Integer>(messages.size());
        for(JSONObject message : messages) {
            final String type = message.getString("type");
            Integer count = messageCountByType.get(type);
            if(count == null) {
                messageCountByType.put(type, 1);
            } else {
                messageCountByType.put(type, count + 1);
            }
        }
        for(Entry<String, Integer> entry : messageCountByType.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
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
    
    public JSONObject getNextPage(final JSONObject feed) throws JSONException, MalformedURLException, IOException {
        final String nextUrl = feed.getJSONObject("paging").getString("next");
        return getFeed(new URL(nextUrl));
    }
    
    public JSONObject getFeed(final URL baseUrl) throws IOException, JSONException {
        final InputStream is = baseUrl.openStream();
        final int blockSize = 8192;
        final byte[] chunk = new byte[blockSize];
        int chunkSize;
        final StringBuffer rawJson = new StringBuffer();
        while ((chunkSize = is.read(chunk)) != -1) {
            rawJson.append(new String(chunk, 0, chunkSize));
        }
        return new JSONObject(rawJson.toString());
    }

}
