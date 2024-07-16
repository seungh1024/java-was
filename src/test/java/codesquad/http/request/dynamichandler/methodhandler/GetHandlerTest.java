package codesquad.http.request.dynamichandler.methodhandler;

import codesquad.command.CommandManager;
import codesquad.command.domain.member.MemberDomain;
import codesquad.http.HttpStatus;
import codesquad.http.request.dynamichandler.DynamicHandleResult;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;
import codesquad.session.Session;
import codesquad.session.SessionUserInfo;
import codesquad.util.FileExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GetHandlerTest {

    @BeforeAll
    static void setUp() {
        CommandManager.getInstance().initMethod(MemberDomain.class);
    }
    @Nested
    @DisplayName("Get 메소드 핸들링 테스트")
    class HandlerTest {
        @Test
        @DisplayName("동적 get 메소드 핸들링 테스트")
        void request_with_query_parameter(){
            // given
            SessionUserInfo sessionUserInfo = new SessionUserInfo(1,"testId", "testName");
            String sessionKey = Session.getInstance().setSession(sessionUserInfo);
            Cookie cookie = new Cookie("sessionKey", sessionKey);
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "/user/list", FileExtension.DYNAMIC, "HTTP/1.1", Map.of(), Map.of("sessionKey",cookie), "");


            // when
            DynamicHandleResult dynamicHandleResult = GetHandler.getInstance().doGet(httpRequest);

            // then
            assertEquals(HttpStatus.OK, dynamicHandleResult.httpStatus());
            assertTrue(dynamicHandleResult.hasBody());
            assertEquals(FileExtension.HTML,dynamicHandleResult.fileExtension());
        }
    }

}