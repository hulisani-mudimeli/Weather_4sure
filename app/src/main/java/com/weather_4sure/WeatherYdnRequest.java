package com.weather_4sure;

//import com.google.android.gms.common.api.Response;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.JsonSyntaxException;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class WeatherYdnRequest<T> extends JsonRequest<T> {
    final String appId = "A0629uvE";
    final String CONSUMER_KEY = "dj0yJmk9NHBXMTZRelRVRWplJmQ9WVdrOVFUQTJNamwxZGtVbWNHbzlNQT09JnM9Y29uc3VtZXJzZWNyZXQmc3Y9MCZ4PWZi";
    final String CONSUMER_SECRET = "eb6c673b5a746060cde81440505fd6ee9d0a4f49";
    final String baseUrl = "https://weather-ydn-yql.media.yahoo.com/forecastrss";

    public WeatherYdnRequest(int method, String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        OAuthConsumer consumer = new OAuthConsumer(null, CONSUMER_KEY, CONSUMER_SECRET, null);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        try {
            OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, getUrl(), null);
            String authorization = request.getAuthorizationHeader(null);
            headers.put("Authorization", authorization);
        } catch (OAuthException | IOException | URISyntaxException e) {
            throw new AuthFailureError(e.getMessage());
        }

        headers.put("X-Yahoo-App-Id", appId);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public String getUrl() {
        return baseUrl + "?location=sunnyvale,ca&format=json";
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            T parsedResponse = parseResponse(json);
            return Response.success(
                    parsedResponse,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    private T parseResponse(String jsonObject) {
        return null; // Add response parsing here
    }
}
