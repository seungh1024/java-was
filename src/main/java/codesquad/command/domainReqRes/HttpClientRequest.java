package codesquad.command.domainReqRes;

import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;
import codesquad.session.SessionUserInfo;
import codesquad.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class HttpClientRequest {
    private Map<String,String> headers;
    private Map<String, Cookie> cookies;
    private String uri;
    private SessionUserInfo userInfo;
    private InputStream inputStream;
    private OutputStream outputStream;


    public HttpClientRequest(HttpRequest httpRequest) {
        this.headers = httpRequest.headers();
        this.cookies = httpRequest.cookie();
        this.uri = httpRequest.uri();
        this.inputStream = httpRequest.inputStream();
        this.outputStream = httpRequest.outputStream();
    }

    public Cookie getCookie(String cookieName) {
        return cookies.get(cookieName);
    }

    public String getUri() {
        return uri;
    }

    public SessionUserInfo getUserInfo() {
        return this.userInfo;
    }
    public void setUserInfo(SessionUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getBoundary() {
        var contentType = this.headers.get("Content-Type");
        if (contentType != null) {
            return "--"+contentType.split(StringUtils.BOUNDARY)[1];
        }
        return null;
    }
}
