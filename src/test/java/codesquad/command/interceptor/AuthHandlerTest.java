package codesquad.command.interceptor;

import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;
import codesquad.session.Session;
import codesquad.session.SessionUserInfo;
import codesquad.util.FileExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static codesquad.util.StringUtils.SESSIONKEY;
import static org.junit.jupiter.api.Assertions.*;

class AuthHandlerTest {

    @Nested
    @DisplayName("사용자 인증 테스트")
    class AuthTest {

        @Test
        @DisplayName("로그인한 사용자라면 true를 리턴한다")
        void request_with_login_user(){
            // given
            SessionUserInfo sessionUserInfo = new SessionUserInfo(1,"testId", "testName");
            String sessionKey = Session.getInstance().setSession(sessionUserInfo);
            Cookie cookie = new Cookie(SESSIONKEY, sessionKey);
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/user/list", FileExtension.DYNAMIC, "HTTP/1.1", Map.of(), Map.of(SESSIONKEY,cookie), "",null,0,null,null);

            // when
            boolean result = AuthHandler.getInstance().handle(httpRequest);

            // then
            assertEquals(true, result);
        }

        @Test
        @DisplayName("로그인을 하지 않은 사용자라면 false를 리턴한다")
        void request_with_non_login_user(){
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/user/list", FileExtension.DYNAMIC, "HTTP/1.1", Map.of(), Map.of(), "",null,0,null,null);

            // when
            boolean result = AuthHandler.getInstance().handle(httpRequest);

            // then
            assertEquals(false, result);
        }

    }
}