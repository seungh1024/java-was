package codesquad.http.request.dynamichandler.methodhandler;

import codesquad.command.CommandManager;
import codesquad.command.domain.user.UserDomain;
import codesquad.db.user.Member;
import codesquad.http.HttpStatus;
import codesquad.http.request.dynamichandler.DynamicHandleResult;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.util.FileExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PostHandlerTest {

    @BeforeAll
    static void setUp() {
        CommandManager.getInstance().initMethod(UserDomain.class);
    }
    @Nested
    @DisplayName("post 메소드 핸들링 테스트")
    class HandlerTest {
        @Test
        @DisplayName("동적 post 메소드 핸들링 테스트")
        void request_with_dynamic_handle_post(){
            // given
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, "/user/create", FileExtension.DYNAMIC, "HTTP/1.1", Map.of(), Map.of(), "userId=test&password=test&name=test&email=test%40test");

            // when
            DynamicHandleResult dynamicHandleResult = GetHandler.getInstance().doGet(httpRequest);

            System.out.println(dynamicHandleResult);
            // then
            assertEquals(HttpStatus.FOUND, dynamicHandleResult.httpStatus());
            assertTrue(dynamicHandleResult.hasBody());
            assertEquals(FileExtension.HTML, dynamicHandleResult.fileExtension());
            assertEquals(new Member("test","test","test","test@test"),dynamicHandleResult.body());
        }
    }
}