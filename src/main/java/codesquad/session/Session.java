package codesquad.session;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
    private static final Session session = new Session();
    private static final Map<Integer, SessionUserInfo> sessionInfo = new ConcurrentHashMap<>();

    private Session() {

    }

    public static Session getInstance() {
        return session;
    }
    public void setSession() {

    }

    public void setCookie(String key, String value) {

    }

}
