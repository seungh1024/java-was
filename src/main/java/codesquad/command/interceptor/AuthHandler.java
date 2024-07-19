package codesquad.command.interceptor;

import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;
import codesquad.session.Session;
import codesquad.util.StringUtils;

import java.util.Objects;

import static codesquad.util.StringUtils.SESSIONKEY;

public class AuthHandler implements PreHandler{
    private static final AuthHandler authHandler = new AuthHandler();
    private AuthHandler() {}
    public static AuthHandler getInstance() {
        return authHandler;
    }

    @Override
    public boolean handle(HttpRequest httpRequest) {
        var cookieInfo = httpRequest.cookie();
        Cookie cookie = cookieInfo.get(SESSIONKEY);
        boolean result = true;
        if (!Objects.isNull(cookie)) {
            var sessionUserInfo = Session.getInstance().getSession(cookie.value());
            if (Objects.isNull(sessionUserInfo)) {
                result = false;
            }
        } else {
            result = false;
        }

        return result;
    }
}
