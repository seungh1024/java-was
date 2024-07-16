package codesquad.db.user;

import codesquad.session.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryTest {

	MemberRepository repository = MemberRepository.getInstance();

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
		} else {
			MemberRepository.getInstance().save(new Member(userId, password, userName, userEmail));
		}
	}


	@Nested
	@DisplayName("사용자 CRUD 테스트")
	class MemberCRUDTest {

		@Test
		@DisplayName("사용자의 정보가 알맞게 입력되면 정상 생성된다.")
		void request_with_valid_user_info() {
			Member member = new Member(userId, password, userName, userEmail);

			Member saveMember = repository.save(member);

			Member findMember = repository.findById(userId);
			assertEquals(findMember,saveMember);
		}
	}

}