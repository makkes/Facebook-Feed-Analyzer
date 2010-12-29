package de.maxwerner.ffa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public interface FeedAnalyzer {
    
    public Long getPageId() throws MalformedURLException, IOException, JSONException;
    
    public List<JSONObject> getMessagesBetween(final JSONObject feed, final Date from, final Date to) throws ParseException,
            JSONException, MalformedURLException, IOException;

    public JSONObject getNextPage(final JSONObject feed) throws JSONException, MalformedURLException, IOException;

    public JSONObject getFeed() throws IOException, JSONException;
    
    public Long getCommentCount(JSONObject message) throws JSONException;
    
    public Long getLikeCount(JSONObject message) throws JSONException;
}
