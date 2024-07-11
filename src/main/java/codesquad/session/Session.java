package codesquad.session;

import codesquad.exception.server.ServerErrorCode;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
    private static final Session session = new Session();
    private static final Map<String, SessionUserInfo> sessionInfo = new ConcurrentHashMap<>();

    private Session() {

    }

    public static Session getInstance() {
        return session;
    }
    public String setSession(SessionUserInfo sessionUserInfo) {
        System.out.println("session");
        String sessionKey = UUID.randomUUID().toString();
        int range = 5;
        boolean isSuccess = false;

        while (range-- > 0) {
            // null이 아니면 기존에 값이 있다는 것
            if (!Objects.isNull(sessionInfo.putIfAbsent(sessionKey, sessionUserInfo))) {
                isSuccess = true;
                break;
            }
        }

        // 5번만에 성공 못하면 예외 발생
        if (!isSuccess) {
            throw ServerErrorCode.CANNOT_CREATE_SESSION.exception();
        }

        return sessionKey;
    }

    public SessionUserInfo getSession(String cookie) {
        return sessionInfo.get(cookie);
    }

    public void removeSession(String cookie) {
        sessionInfo.remove(cookie);
    }


}
