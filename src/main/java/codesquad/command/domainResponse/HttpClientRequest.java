package codesquad.command.domainResponse;

import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;

import java.util.Map;

public class HttpClientRequest {
    Map<String,String> headers;
    Map<String, Cookie> cookies;

    public HttpClientRequest(HttpRequest httpRequest) {
        this.headers = httpRequest.headers();
        this.cookies = httpRequest.cookie();
    }

    public Cookie getCookie(String cookieName) {
        return cookies.get(cookieName);
    }
}
