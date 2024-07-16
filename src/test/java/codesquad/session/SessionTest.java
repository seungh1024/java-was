package codesquad.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

	@Test
	@DisplayName("세션 생성, 조회, 삭제 테스트")
	void session_create_select_delete() {
		// given
		var sessionUserInfo = new SessionUserInfo(1,"userId", "userName");
		var cookie = Session.getInstance().setSession(sessionUserInfo);

		// when
		var getUserInfo = Session.getInstance().getSession(cookie);
		Session.getInstance().removeSession(cookie);

		// then
		assertEquals(sessionUserInfo,getUserInfo);
		assertNull(Session.getInstance().getSession(cookie));
	}
}