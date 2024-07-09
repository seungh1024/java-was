package codesquad.command.domainResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpClientResponse {
    private Map<String,String> cookie;

    public HttpClientResponse(Map<String,String> cookie) {
        this.cookie = cookie;
    }

    public void setCookie(String key, String value) {
        encrypt(value);
        this.cookie.put(key, value);
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
