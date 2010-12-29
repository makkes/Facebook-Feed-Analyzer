package de.maxwerner.ffa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import de.maxwerner.ffa.impl.FacebookFeedAnalyzer;

public class Application {
    
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    public static void main(String[] args) throws IOException, JSONException, ParseException {
        final String profileName = args[0];
        final Date fromDate = DATE_FORMAT.parse(args[1]);
        final Date toDate = DATE_FORMAT.parse(args[2]);
        
        System.out.println("Analyzing feed of '" + profileName + "' in date range " + fromDate + " - " + toDate);
        
        final FacebookFeedAnalyzer analyzer = new FacebookFeedAnalyzer(profileName);
//        analyzeFeed(analyzer, fromDate, toDate);
        analyzeFollowerActivity(analyzer, fromDate, toDate);
    }
    
    public static void analyzeFollowerActivity(final FeedAnalyzer analyzer, final Date from, final Date to)
            throws MalformedURLException, IOException, JSONException, ParseException {
        
        final JSONObject feed = analyzer.getFeed();
        final List<JSONObject> messages = analyzer.getMessagesBetween(feed, from, to);
        final Set<Long> userIds = new HashSet<Long>();
        final Long ownerId = analyzer.getPageId();
        long postsFromOthers = 0;
        long uniqueUsers = 0;
        for(final JSONObject message : messages) {
            final Long fromId = new Long(message.getJSONObject("from").getLong("id"));
            if(!(fromId.equals(ownerId))) {
                if(!userIds.contains(fromId)) {
                    uniqueUsers++;
                    userIds.add(fromId);
                }
                postsFromOthers++;
            }
        }
        System.out.println(postsFromOthers);
        System.out.println(uniqueUsers);
    }
    
    public static void analyzeFeed(final FeedAnalyzer analyzer, final Date from, final Date to) throws IOException,
            JSONException, ParseException {
        
        final JSONObject feed = analyzer.getFeed();

        final List<JSONObject> messages = analyzer.getMessagesBetween(feed, from, to);
        System.out.println("Total messages: " + messages.size());
        
        final Map<String, Integer> messageCountByType = new HashMap<String, Integer>(messages.size());
        Long commentCount = 0L;
        Long likeCount = 0L;
        for(JSONObject message : messages) {
            commentCount += analyzer.getCommentCount(message);
            likeCount += analyzer.getLikeCount(message);
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
        System.out.println("comments: " + commentCount);
        System.out.println("likes: " + likeCount);
    }

}
