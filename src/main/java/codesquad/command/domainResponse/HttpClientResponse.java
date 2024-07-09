package codesquad.command.domainResponse;

import java.util.HashMap;
import java.util.Map;

public class HttpClientResponse {
    private Map<String,String> cookie;

    public HttpClientResponse() {
        this.cookie = new HashMap<>();
    }

    public void setCookie(String key, String value) {
        this.cookie.put(key, value);
    }
}
