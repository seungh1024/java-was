package codesquad.command.interceptor;

import codesquad.exception.client.ClientErrorCode;
import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class AuthHandler implements PreHandler{
    private static final AuthHandler authHandler = new AuthHandler();
    private AuthHandler() {}
    public static AuthHandler getInstance() {
        return authHandler;
    }

    @Override
    public boolean handle(HttpRequest httpRequest) {
        var cookieInfo = httpRequest.cookie();
        Cookie cookie = cookieInfo.get("sessionKey");

        boolean result = true;
        if (Objects.isNull(cookie)) {
//            throw ClientErrorCode.UNAUTHORIZED_USER.exception();
            result = false;
        }

        return result;
    }
}
