package codesquad.http.request.dynamichandler.methodhandler;

import codesquad.command.CommandManager;
import codesquad.command.domain.user.MemberDomain;
import codesquad.db.user.Member;
import codesquad.db.user.MemberRepository;
import codesquad.http.HttpStatus;
import codesquad.http.request.dynamichandler.DynamicHandleResult;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.session.Session;
import codesquad.util.FileExtension;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PostHandlerTest {

    String userId = "testId";
    String password = "password";
    String userName = "testName";
    String userEmail = "testEmail@naver.com";

    @BeforeEach
    void setUp() {
        CommandManager.getInstance().initMethod(MemberDomain.class);
        Session.getInstance().removeSession("value");

        Member member = MemberRepository.getInstance().findById(userId);
        if (member != null) {
            MemberRepository.getInstance().delete(member);
        } else {
            MemberRepository.getInstance().save(new Member(userId, password, userName, userEmail));
        }
    }

    @Nested
    @DisplayName("post 메소드 핸들링 테스트")
    class HandlerTest {
        @Test
        @DisplayName("동적 post 메소드 핸들링 테스트")
        void request_with_dynamic_handle_post(){
            // given
            String body = "userId="+userId+"&password="+password+"&name="+userName+"&email="+userEmail;
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, "/user/create", FileExtension.DYNAMIC, "HTTP/1.1", Map.of(), Map.of(), body);

            // when
            DynamicHandleResult dynamicHandleResult = GetHandler.getInstance().doGet(httpRequest);

            System.out.println(dynamicHandleResult);
            // then
            assertEquals(HttpStatus.FOUND, dynamicHandleResult.httpStatus());
            assertTrue(dynamicHandleResult.hasBody());
            assertEquals(FileExtension.HTML, dynamicHandleResult.fileExtension());
            assertEquals(new Member(userId,password,userName,userEmail),dynamicHandleResult.body());
        }
    }
}