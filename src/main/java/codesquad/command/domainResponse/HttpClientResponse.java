package codesquad.command.domainResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class HttpClientResponse {
    private Map<String,String> headers;
    private Map<String,String> cookie;
    private Map<String, List<String>> cookieOptions;

    public HttpClientResponse() {
        this.headers = new HashMap<>();
        this.cookie = new HashMap<>();
        this.cookieOptions = new HashMap<>();
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public Map<String, String> getCookie() {
        return this.cookie;
    }
    public void setCookie(String key, String value) {
        encrypt(value);
        this.cookie.put(key, value);
    }

    public void setMaxAge(String key, int value) {
        if (cookieOptions.containsKey(key)) {
            cookieOptions.get(key).add("Max-Age="+value);
        } else {
            cookieOptions.put(key, new ArrayList<>(List.of("Max-Age="+value)));
        }
    }

    public Map<String, List<String>> getCookieOptions() {
        return this.cookieOptions;
    }

    public byte[] encrypt(String value) {
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(value.getBytes());
            byte[] digest = messageDigest.digest();
            System.out.println("digest = "+ Arrays.toString(digest));
            return digest;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
