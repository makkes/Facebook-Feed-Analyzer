package de.maxwerner.ffa;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import de.maxwerner.ffa.impl.FacebookFeedAnalyzer;

public class Application {
    
    public static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final FacebookFeedAnalyzer analyzer = new FacebookFeedAnalyzer();
    
    public static void main(String[] args) throws IOException, JSONException, ParseException {
        final URL baseUrl = new URL(args[0]);
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

}
