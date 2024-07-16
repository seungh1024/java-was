package codesquad.command.domainResponse;

import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;

import java.util.Map;

public class HttpClientRequest {
    Map<String,String> headers;
    Map<String, Cookie> cookies;
    String uri;

    public HttpClientRequest(HttpRequest httpRequest) {
        this.headers = httpRequest.headers();
        this.cookies = httpRequest.cookie();
        this.uri = httpRequest.uri();
    }

    public Cookie getCookie(String cookieName) {
        return cookies.get(cookieName);
    }
}
