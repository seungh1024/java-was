package codesquad.command.domainResponse;

import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;
import codesquad.session.SessionUserInfo;

import java.util.Map;

public class HttpClientRequest {
    Map<String,String> headers;
    Map<String, Cookie> cookies;
    String uri;
    SessionUserInfo userInfo;

    public HttpClientRequest(HttpRequest httpRequest) {
        this.headers = httpRequest.headers();
        this.cookies = httpRequest.cookie();
        this.uri = httpRequest.uri();
    }

    public Cookie getCookie(String cookieName) {
        return cookies.get(cookieName);
    }

    public SessionUserInfo getUserInfo() {
        return this.userInfo;
    }
    public void setUserInfo(SessionUserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
