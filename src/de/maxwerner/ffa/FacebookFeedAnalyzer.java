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
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 *
 * @author Max Jonas Werner <max.jonas.werner@justsoftwareag.com>
 */
public class FacebookFeedAnalyzer {

    private final static DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private final static DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void main(String[] args) throws IOException, JSONException, ParseException {
        final FacebookFeedAnalyzer analyzer = new FacebookFeedAnalyzer();
        final URL baseUrl = new URL("https://graph.facebook.com/weltkompakt/feed");
        final JSONObject feed = analyzer.getFeed(baseUrl);
        final JSONArray feedData = feed.getJSONArray("data");

        final Map<String, Integer> commentsPerType = new HashMap<String, Integer>();
        final Map<String, Integer> postsPerType = new HashMap<String, Integer>();

        for (int i = 0; i < feedData.length(); i++) {
            final JSONObject entry = feedData.getJSONObject(i);
            final String type = entry.getString("type");
            
            final JSONObject comments = entry.optJSONObject("comments");
            final Integer commentCount = comments == null ? 0 : new Integer(comments.getInt("count"));
            if(entry.getJSONObject("from").getString("id").equals("49522294277")) {
                postsPerType.put(type, postsPerType.get(type) == null ? 1 : postsPerType.get(type) + 1);
            }

            commentsPerType.put(type, commentsPerType.get(type) != null ? 
                    commentsPerType.get(type) + commentCount
                    : commentCount);
        }

        for(String type : postsPerType.keySet()) {
            System.out.println(type + ": " + postsPerType.get(type) + ", " + commentsPerType.get(type));
        }

    }

    public Set<String> getTypesInFeed(final JSONArray feed) throws JSONException {
        final Set<String> types = new HashSet<String>();
        for(int i = 0; i < feed.length(); i++) {
            final JSONObject entry = feed.getJSONObject(i);
            if(!types.contains(entry.getString("type"))) {
                types.add(entry.getString("type"));
            }
        }
        return types;
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
