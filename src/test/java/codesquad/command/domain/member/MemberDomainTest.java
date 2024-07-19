package codesquad.command.domain.member;

import java.util.List;
import java.util.Map;

import codesquad.command.domain.DynamicResponseBody;
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

import static codesquad.util.StringUtils.SESSIONKEY;
import static org.junit.jupiter.api.Assertions.*;

class MemberDomainTest {

	String userId = "testId";
	String password = "password";
	String userName = "testName";
	String userEmail = "testEmail@naver.com";

	@BeforeEach
	void init() {
		Session.getInstance().removeSession("value");

		Member member = MemberRepository.getInstance().findById(userId);
		if (member != null) {
			MemberRepository.getInstance().delete(member);
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
			MemberRepository.getInstance().save(userInfo);

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
			MemberRepository.getInstance().save(userInfo);
			var httpClientResponse = new HttpClientResponse();

			// when
			MemberDomain.getInstance().login(userId, password, httpClientResponse);

			// then
			var cookieInfo = httpClientResponse.getCookie();
			var sessionKey = cookieInfo.get(SESSIONKEY);
			var session = Session.getInstance().getSession(sessionKey);
			assertEquals(userId, session.userId());
			assertEquals(userName, session.userName());
		}

		@Test
		@DisplayName("사용자 정보가 존재하지 않으면 예외가 발생한다.")
		void request_with_incorrect_user_info() {
			// given
			var httpClientResponse = new HttpClientResponse();
			Member member = new Member(userId, password, userName, userEmail);
			MemberRepository.getInstance().delete(member);

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
			var sessionUserInfo = new SessionUserInfo(1, userId, userName);
			String value = Session.getInstance().setSession(sessionUserInfo);

			var httpClientRequest = new HttpClientRequest(
				new HttpRequest(HttpMethod.POST, "testUri", FileExtension.HTML, "http 1.1", Map.of(),
					Map.of(SESSIONKEY, new Cookie("key", "value")), "body"));
			var httpClientResponse = new HttpClientResponse();
			httpClientResponse.setCookie(SESSIONKEY, value);

			// when
			MemberDomain.getInstance().logout(httpClientRequest, httpClientResponse);

			// then
			var session = Session.getInstance()
				.getSession(httpClientRequest.getCookie(SESSIONKEY).value());
			assertNull(session);

		}
	}

	@Nested
	@DisplayName("사용자 리스트 테스트")
	class MemberListTest{

		@Test
		@DisplayName("로그인한 사용자는 사용자 리스트를 반환받는다.")
		void request_with_login(){
			// given
			Member member = new Member("loginTest", "password", "name", "email");
			Member findMember = MemberRepository.getInstance().findById("loginTest");
			if (findMember != null) {
				MemberRepository.getInstance().delete(findMember);
			}
			Member saveMember = MemberRepository.getInstance().save(member);


			var sessionUserInfo = new SessionUserInfo(1, userId, userName);
			var httpClientRequest = new HttpClientRequest(
				new HttpRequest(HttpMethod.POST, "testUri", FileExtension.HTML, "http 1.1", Map.of(),
					Map.of(), "body"));
			httpClientRequest.setUserInfo(sessionUserInfo);

			// when
			String userList = MemberDomain.getInstance().getUserList(httpClientRequest);

			// then
			List<Member> memberList = MemberRepository.getInstance().findMemberList();
			String userListHtml = DynamicResponseBody.getInstance()
				.getUserListHtml("/dynamic/userList.html", sessionUserInfo, memberList);
			MemberRepository.getInstance().delete(saveMember);
			assertEquals(userListHtml, userList);
		}

	}

	@Nested
	@DisplayName("html 페이지 조회 테스트")
	class MemberHtmlText {

		@Test
		@DisplayName("회원가입 페이지 요청을 하면 회원가입 페이지를 응답한다.")
		void request_of_registration_page() {
			// when
			String registrationHtml = MemberDomain.getInstance().getRegistrationHtml();

			// then
			String expectedHtml = DynamicResponseBody.getInstance().getHtmlFile("/registration/index.html", null);
			assertEquals(expectedHtml,registrationHtml);
		}

		@Test
		@DisplayName("로그인 페이지 요청을 하면 로그인 페이지를 응답한다.")
		void request_of_login_page() {
			// when
			String loginHtml = MemberDomain.getInstance().getLoginHtml();

			// then
			String expectedHtml = DynamicResponseBody.getInstance().getHtmlFile("/login/index.html", null);
			assertEquals(expectedHtml, loginHtml);
		}
	}
}