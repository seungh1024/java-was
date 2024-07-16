package codesquad.command.domain.user;

import java.util.Map;

import codesquad.db.DBConnectionUtil;
import codesquad.db.user.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import codesquad.command.domainResponse.HttpClientRequest;
import codesquad.command.domainResponse.HttpClientResponse;
import codesquad.db.user.Member;
import codesquad.exception.CustomException;
import codesquad.exception.client.ClientErrorCode;
import codesquad.http.request.format.HttpMethod;
import codesquad.http.request.format.HttpRequest;
import codesquad.session.Cookie;
import codesquad.session.Session;
import codesquad.session.SessionUserInfo;
import codesquad.util.FileExtension;

import static org.junit.jupiter.api.Assertions.*;

class UserDomainTest {

	String userId = "testId";
	String password = "password";
	String userName = "testName";
	String userEmail = "testEmail@naver.com";
	Cookie cookie = new Cookie("key", "value");

	@BeforeEach
	void init() {
		Session.getInstance().removeSession("value");

		Member member = MemberRepository.getInstance().findById(userId);
		if (member != null) {
			MemberRepository.getInstance().delete(member);
		} else {
			MemberRepository.getInstance().save(new Member(userId, password, userName, userEmail));
		}
	}

	@Nested
	@DisplayName("사용자 생성 테스트")
	class CreateUserTest {

		@Test
		@DisplayName("아이디가 중복되지 않으면 사용자 생성에 성공한다.")
		void request_with_another_user_id() {
			// given
			Member member = new Member(userId, password, userName, userEmail);
			MemberRepository.getInstance().delete(member);

			// when
			var user = MemberDomain.getInstance().createUser(userId, password, userName, userEmail);

			// then
			assertEquals(member, user);
		}

		@Test
		@DisplayName("중복된 아이디의 사용자가 있으면 사용자 생성에 실패한다")
		void request_with_same_user_id() {
			// given
			var userInfo = new Member(userId, password, userName, userEmail);

			// when
			var customException = assertThrows(CustomException.class, () -> {
				MemberDomain.getInstance().createUser(userId, password, userName, userEmail);
			});

			// then
			assertEquals(ClientErrorCode.USERID_ALREADY_EXISTS.exception().getStatusCode(),
				customException.getStatusCode());
			assertEquals(ClientErrorCode.USERID_ALREADY_EXISTS.exception().getErrorName(),
				customException.getErrorName());
		}
	}

	@Nested
	@DisplayName("로그인 테스트")
	class LoginTest {

		@Test
		@DisplayName("사용자 정보가 존재하면 로그인에 성공한다.")
		void request_with_correct_user_info() {
			// given
			var userInfo = new Member(userId, password, userName, userEmail);
			var httpClientResponse = new HttpClientResponse();

			// when
			MemberDomain.getInstance().login(userId, password, httpClientResponse);

			// then
			var cookieInfo = httpClientResponse.getCookie();
			var sessionKey = cookieInfo.get("sessionKey");
			var session = Session.getInstance().getSession(sessionKey);
			assertEquals(userId, session.userId());
			assertEquals(userName, session.userName());
		}

		@Test
		@DisplayName("사용자 정보가 존재하지 않으면 예외가 발생한다.")
		void request_with_incorrect_user_info() {
			// given
			var httpClientResponse = new HttpClientResponse();

			// when
			var customException = assertThrows(CustomException.class, () -> {
				MemberDomain.getInstance().login(userId, password, httpClientResponse);
			});

			//then
			assertEquals(ClientErrorCode.USER_NOT_FOUND.exception().getStatusCode(), customException.getStatusCode());
			assertEquals(ClientErrorCode.USER_NOT_FOUND.exception().getErrorName(), customException.getErrorName());

		}
	}

	@Nested
	@DisplayName("로그아웃 테스트")
	class LogoutTest {
		@Test
		@DisplayName("로그아웃을 하면 세션 정보가 사라져야 한다")
		void session_info_deleted_after_logout() {
			// given
			var sessionUserInfo = new SessionUserInfo(1,userId, userName);
			String value = Session.getInstance().setSession(sessionUserInfo);

			var httpClientRequest = new HttpClientRequest(
				new HttpRequest(HttpMethod.POST, "testUri", FileExtension.HTML, "http 1.1", Map.of(),
					Map.of("sessionKey", new Cookie("key", "value")), "body"));
			var httpClientResponse = new HttpClientResponse();
			httpClientResponse.setCookie("sessionKey", value);

			// when
			MemberDomain.getInstance().logout(httpClientRequest, httpClientResponse);

			// then
			var session = Session.getInstance()
				.getSession(httpClientRequest.getCookie("sessionKey").value());
			assertNull(session);

		}
	}


}